package rcwd.service;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.exec.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rcwd.helper.MessageHelper;
import rcwd.helper.ProcessResultHandler;
import rcwd.helper.ProcessingLogOutputStream;
import rcwd.mapper.CommandMapper;
import rcwd.mapper.StatusMapper;
import rcwd.model.Command;
import rcwd.model.StatusEnum;
import rcwd.properties.RcwdProperties;
import rcwd.properties.TelegramProperties;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.apache.commons.exec.ExecuteWatchdog.INFINITE_TIMEOUT;

@Service
@Log4j2
public class ExecutionService {

    @Autowired
    private RcwdProperties properties;

    @Autowired
    private TelegramProperties telegramProperties;
    
    @Autowired
    private TelegramService telegramService;

    @Autowired
    private CommandMapper commandMapper;

    @Autowired
    private StatusMapper statusMapper;

    private Map<Long, CircularFifoQueue<String>> logs = new HashMap<>();
    private Map<Long, DefaultExecutor> executors = new HashMap<>();

    private MessageHelper messageHelper;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledBandwidthResetJob = null;

    public void execute(List<Command> commands) {
        for (Command command : commands) {
            execute(command, false);
        }
    }

    /**
     * Perform a dry run of the execution and write the rclone output to the output stream.
     */
    public void dryRun(Command command, OutputStream outputStream){
        statusMapper.insert(command.getId(), StatusEnum.DRY_RUN_EXECUTION_START, null);
        CommandLine cmdLine = command.getCommandLine(properties.getRcloneBasePath().trim());
        cmdLine.addArgument("--dry-run");
        log.debug(cmdLine.toString());
        DefaultExecutor executor = new DefaultExecutor();
        executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        executor.setStreamHandler(streamHandler);
        try {
            executor.execute(cmdLine);
            statusMapper.insert(command.getId(), StatusEnum.DRY_RUN_EXECUTION_SUCCESS, null);
        } catch (IOException e) {
            log.error("Exception during dry run", e);
            statusMapper.insert(command.getId(), StatusEnum.DRY_RUN_EXECUTION_FAIL, null);
        }
    }

    /**
     * @param spawnNewThread if true, a new thread will be spawned and this method will return immediately,
     *                       executing the command in the background. if false, this will execute in the current
     *                       thread and this method will only return once the execution finishes
     */
    public void execute(Command command, boolean spawnNewThread) {
        long startTime = System.nanoTime();
        log.debug("Begin executing " + command.getId());
        statusMapper.insert(command.getId(), StatusEnum.EXECUTION_START, null);
        log.debug("Executing in " + properties.getCurrentDirectory());
        //verifyRcloneNotAlreadyRunning();

        if(messageHelper == null){
            messageHelper = new MessageHelper(properties.getMaxTelegramLogLines());
        }

        telegramService.sendTelegramMessage(messageHelper.buildTelegramExecutionStartText(command.getName()));

        CircularFifoQueue<String> logQueue = getLogQueueForCommand(command.getId());
        CommandLine cmdLine = command.getCommandLine(properties.getRcloneBasePath().trim());
        cmdLine.addArgument("--verbose");
        cmdLine.addArgument("--delete-before");
        cmdLine.addArgument("--delete-excluded");
        cmdLine.addArgument("--rc");
        if (StringUtils.isNotEmpty(properties.getBandwidthSchedule()) &&
                !properties.getBandwidthSchedule().contains("$") &&
                !properties.getBandwidthSchedule().contains("@")) {
            cmdLine.addArgument("--bwlimit=" + properties.getBandwidthSchedule(), false);
        }
        DefaultExecutor executor = getExecutorForCommand(command.getId(), true);
        ProcessingLogOutputStream logOutputStream = new ProcessingLogOutputStream(telegramService, command.getName(), logQueue, properties.getMaxTelegramLogLines(), properties.getPrintRcloneToConsole());
        PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(logOutputStream);
        executor.setStreamHandler(pumpStreamHandler);
        try {
            if (spawnNewThread) {
                ProcessResultHandler resultHandler = new ProcessResultHandler(messageHelper, telegramService, statusMapper, command, logQueue, startTime);
                executor.execute(cmdLine, resultHandler);
            } else {
                executor.execute(cmdLine);
                telegramService.sendTelegramMessage(messageHelper.buildTelegramExecutionEndText(command.getName(), startTime, System.nanoTime(), logQueue));
                statusMapper.insert(command.getId(), StatusEnum.EXECUTION_SUCCESS, null);
            }
        } catch (IOException e) {
            telegramService.sendTelegramMessage(messageHelper.buildFailureText(command.getName(), e.toString(), logQueue));
            log.error("Failed to run rclone job", e);
            statusMapper.insert(command.getId(), StatusEnum.EXECUTION_FAIL, null);
        }

        log.debug("Finish executing " + command.getId());
    }

    /**
     * @param timeToWaitBeforeResettingToDefault if not null, the bandwidth limit will be reset back to the default value after this time
     * @param unit the unit that the timeToWaitBeforeResettingToDefault param is in
     */
    public int setBandwidthLimit(String limit, Long timeToWaitBeforeResettingToDefault, TimeUnit unit) throws Exception {
        if (limit.length() > 4) {
            throw new Exception("Bandwidth limit cannot be longer than 4 characters.");
        }
        int exitValue = setBandwidthLimit(limit);
        if(exitValue == 0){
            // also schedule a delayed job to reset the bandwidth back to default, if requested
            if (timeToWaitBeforeResettingToDefault != null) {
                telegramService.sendTelegramMessage(String.format("Bandwidth limit set to %s for %s %s", limit, timeToWaitBeforeResettingToDefault, unit.toString().toLowerCase()));
                // if there is already a scheduled job, cancel it
                if (scheduledBandwidthResetJob != null) {
                    scheduledBandwidthResetJob.cancel(false);
                }
                // and replace it with a new job
                scheduledBandwidthResetJob = scheduler.schedule(() -> {
                    try {
                        String newLimit = properties.getBandwidthSchedule();
                        telegramService.sendTelegramMessage(String.format("Bandwidth limit set to %s after waiting %s %s", newLimit, timeToWaitBeforeResettingToDefault, unit.toString().toLowerCase()));
                        setBandwidthLimit(newLimit);
                    } catch (Exception e) {
                        telegramService.sendTelegramMessage(String.format("Failed to reset bandwidth limit to default of %s after waiting %s %s.", properties.getBandwidthSchedule(), timeToWaitBeforeResettingToDefault, unit.toString().toLowerCase()));
                    }
                }, timeToWaitBeforeResettingToDefault, unit);
            } else {
                telegramService.sendTelegramMessage(String.format("Bandwidth limit set to %s", limit));
            }
        } else {
            telegramService.sendTelegramMessage(String.format("Failed to set bandwidth limit to %s, exit value %s", limit, exitValue));
        }
        return exitValue;
    }

    /**
     * Immediately set bandwidth limit to requested value.
     */
    private int setBandwidthLimit(String limit) throws IOException {
        CommandLine cmdLine = CommandLine.parse(properties.getRcloneBasePath().trim());
        cmdLine.addArgument("rc");
        cmdLine.addArgument("core/bwlimit");
        cmdLine.addArgument("rate=" + limit);
        DefaultExecutor executor = new DefaultExecutor();
        return executor.execute(cmdLine);
    }

    public void kill(long commandId) {
        getExecutorForCommand(commandId, false).getWatchdog().destroyProcess();
    }

    public CircularFifoQueue<String> getLogQueueForCommand(long commandId){
        CircularFifoQueue<String> queue = logs.get(commandId);
        if(queue == null){
            logs.put(commandId, new CircularFifoQueue<String>(properties.getMaxLogLines()));
        }
        return logs.get(commandId);
    }

    /**
     * Returns the executor associated with a particular command.
     *
     * @param createNew if true, and if no executor exists for the specified command, a new executor will be created
     */
    private DefaultExecutor getExecutorForCommand(long commandId, boolean createNew) {
        DefaultExecutor executor = executors.get(commandId);
        if (executor == null && createNew) {
            executor = new DefaultExecutor();
            executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());
            executor.setWatchdog(new ExecuteWatchdog(INFINITE_TIMEOUT));
            executors.put(commandId, executor);
        }
        return executors.get(commandId);
    }

    // TODO this needs to be de-windowsafied
    /*
    private void verifyRcloneNotAlreadyRunning() throws Exception {
        if (Boolean.TRUE.equals(properties.getPerformMultipleRcloneExecutionCheck())) {
            Runtime rt = Runtime.getRuntime();
            Process p = rt.exec("tasklist.exe");
            String executions = IOUtils.toString(p.getInputStream(), StandardCharsets.UTF_8);

            if (StringUtils.countMatches(executions, "rclone.exe") > properties.getConcurrentRcloneExecutionLimit()) {
                throw new Exception(properties.getConcurrentRcloneExecutionLimit() + " concurrent " +
                        " " + (properties.getConcurrentRcloneExecutionLimit() > 1 ? "instances" : "instance") + " of rclone.exe allowed to be running.");
            }
        }
    }
    */

}
