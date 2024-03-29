package rcwd.service;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.exec.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.test.util.TestSocketUtils;
import rcwd.helper.MessageHelper;
import rcwd.helper.ProcessResultHandler;
import rcwd.helper.ProcessingLogOutputStream;
import rcwd.mapper.StatusMapper;
import rcwd.model.Command;
import rcwd.model.StatusEnum;
import rcwd.properties.RcwdProperties;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.apache.commons.exec.ExecuteWatchdog.INFINITE_TIMEOUT;

@Service
@Log4j2
public class ExecutionService {

    private final RcwdProperties properties;
    private final TelegramService telegramService;
    private final StatusMapper statusMapper;
    private final Map<Long, CircularFifoQueue<String>> logs = new HashMap<>();
    private final Map<Long, DefaultExecutor> executors = new HashMap<>();
    /**
     * A map, where each key is the command ID, and the value is the port number that rc is running on for that
     * command. When execution finishes (with success or error) the value for that command should be removed
     * from this map.
     */
    public static final Map<Long, Integer> rcPorts = new HashMap<>();

    private final MessageHelper messageHelper;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledBandwidthResetJob = null;
    private final boolean ignoreDiscardedClosedSshConnection;

    public ExecutionService(RcwdProperties properties,
                            TelegramService telegramService,
                            StatusMapper statusMapper,
                            @Value("${ignoreDiscardedClosedSshConnection:true}") boolean ignoreDiscardedClosedSshConnection) {
        this.properties = properties;
        this.telegramService = telegramService;
        this.statusMapper = statusMapper;
        this.ignoreDiscardedClosedSshConnection = ignoreDiscardedClosedSshConnection;
        messageHelper = new MessageHelper(properties.getMaxTelegramLogLines());
    }

    public void execute(List<Command> commands) throws Exception {
        for (Command command : commands) {
            execute(command, false, false);
        }
    }

    /**
     * Perform a dry run of the execution and write the rclone output to the output stream.
     * @param force if true, will not check to make sure that rclone is not already running
     */
    public void dryRun(Command command, boolean force) throws Exception {
        statusMapper.insert(command.getId(), StatusEnum.DRY_RUN_EXECUTION_START, null);
        CommandLine cmdLine = command.getCommandLine(properties.getRcloneBasePath().trim());
        cmdLine.addArgument("--dry-run");
        log.debug(cmdLine.toString());
        if (!force) {
            verifyRcloneNotAlreadyRunning(command);
        }

        CircularFifoQueue<String> logQueue = getLogQueueForCommand(command.getId(), 100_000, true);
        ProcessingLogOutputStream logOutputStream = new ProcessingLogOutputStream(this, command, logQueue, properties.getPrintRcloneToConsole(), ignoreDiscardedClosedSshConnection);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());
        PumpStreamHandler streamHandler = new PumpStreamHandler(logOutputStream);
        executor.setStreamHandler(streamHandler);
        try {
            ProcessResultHandler resultHandler = new ProcessResultHandler(messageHelper, telegramService, statusMapper, command, logQueue, null, true);
            executor.execute(cmdLine, resultHandler);
        } catch (IOException e) {
            log.error("Exception during dry run", e);
            statusMapper.insert(command.getId(), StatusEnum.DRY_RUN_EXECUTION_FAIL, null);
            telegramService.sendTelegramMessage(messageHelper.buildTelegramDryRunExecutionEndText(command.getName()));
        }
    }

    /**
     * @param spawnNewThread if true, a new thread will be spawned and this method will return immediately,
     *                       executing the command in the background. if false, this will execute in the current
     *                       thread and this method will only return once the execution finishes
     * @param force if true, will not check to make sure that rclone is not already running
     */
    public void execute(Command command, boolean spawnNewThread, boolean force) throws Exception {
        long startTime = System.nanoTime();
        log.debug("Begin executing " + command.getId());
        statusMapper.insert(command.getId(), StatusEnum.EXECUTION_START, null);
        log.debug("Executing in " + properties.getCurrentDirectory());
        if (!force) {
            verifyRcloneNotAlreadyRunning(command);
        }

        telegramService.sendTelegramMessage(messageHelper.buildTelegramExecutionStartText(command.getName()));

        CircularFifoQueue<String> logQueue = getLogQueueForCommand(command.getId(), true);
        CommandLine cmdLine = command.getCommandLine(properties.getRcloneBasePath().trim());
        cmdLine.addArgument("--verbose");
        cmdLine.addArgument("--delete-before");
        cmdLine.addArgument("--delete-excluded");
        cmdLine.addArgument("--rc");
        // todo, don't use a test class in production code to find available TCP port
        int availableTcpPort = TestSocketUtils.findAvailableTcpPort();
        rcPorts.put(command.getId(), availableTcpPort);
        cmdLine.addArgument("--rc-addr=localhost:" + availableTcpPort);
        if (StringUtils.isNotEmpty(properties.getBandwidthSchedule()) &&
                !properties.getBandwidthSchedule().contains("$") &&
                !properties.getBandwidthSchedule().contains("@")) {
            cmdLine.addArgument("--bwlimit=" + properties.getBandwidthSchedule(), false);
        }
        DefaultExecutor executor = getExecutorForCommand(command.getId(), true);
        ProcessingLogOutputStream logOutputStream = new ProcessingLogOutputStream(this, telegramService, command, logQueue, properties.getMaxTelegramLogLines(), properties.getPrintRcloneToConsole(), ignoreDiscardedClosedSshConnection);
        PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(logOutputStream);
        executor.setStreamHandler(pumpStreamHandler);
        try {
            if (spawnNewThread) {
                ProcessResultHandler resultHandler = new ProcessResultHandler(messageHelper, telegramService, statusMapper, command, logQueue, startTime, false);
                executor.execute(cmdLine, resultHandler);
            } else {
                executor.execute(cmdLine);
                telegramService.sendTelegramMessage(messageHelper.buildTelegramExecutionEndText(command.getName(), startTime, System.nanoTime(), logQueue));
                statusMapper.insert(command.getId(), StatusEnum.EXECUTION_SUCCESS, null);
                rcPorts.remove(command.getId());
                command.sendHealthChecksIoCall(messageHelper, logQueue, false);
            }
        } catch (IOException e) {
            telegramService.sendTelegramMessage(messageHelper.buildFailureText(command.getName(), e.toString(), logQueue));
            log.error("Failed to run rclone job", e);
            statusMapper.insert(command.getId(), StatusEnum.EXECUTION_FAIL, null);
            command.sendHealthChecksIoCall(messageHelper, logQueue, true);
        }

        log.debug("Finish executing " + command.getId());
    }

    /**
     * @param timeToWaitBeforeResettingToDefault if not null, the bandwidth limit will be reset back to the default value after this time
     * @param unit the unit that the timeToWaitBeforeResettingToDefault param is in
     * @return the exit code from setting the bandwidth limit, or -1 if nothing is currently running
     */
    public int setBandwidthLimit(String limit, Long timeToWaitBeforeResettingToDefault, TimeUnit unit) throws Exception {
        if (rcPorts.isEmpty()) {
            return -1;
        }
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
        int exitValues = 0;
        for (Map.Entry<Long, Integer> rcPort : rcPorts.entrySet()) {
            CommandLine cmdLine = CommandLine.parse(properties.getRcloneBasePath().trim());
            cmdLine.addArgument("rc");
            cmdLine.addArgument("core/bwlimit");
            cmdLine.addArgument("rate=" + limit);
            cmdLine.addArgument("--rc-addr=localhost:" + rcPort.getValue());
            DefaultExecutor executor = new DefaultExecutor();
            exitValues += executor.execute(cmdLine);
        }
        return exitValues;
    }

    public void kill(long commandId) {
        getExecutorForCommand(commandId, false).getWatchdog().destroyProcess();
    }

    public CircularFifoQueue<String> getLogQueueForCommand(long commandId, boolean clearExistingLogs){
        return getLogQueueForCommand(commandId, null, clearExistingLogs);
    }

    public CircularFifoQueue<String> getLogQueueForCommand(long commandId, Integer maxLogLines, boolean clearExistingLogs) {
        if (clearExistingLogs) {
            logs.remove(commandId);
        }
        CircularFifoQueue<String> queue = logs.get(commandId);
        if (queue == null) {
            Integer logLines = maxLogLines == null ? properties.getMaxLogLines() : maxLogLines;
            logs.put(commandId, new CircularFifoQueue<>(logLines));
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

    private void verifyRcloneNotAlreadyRunning(Command command) throws Exception {
        if (StatusEnum.EXECUTION_START.getName().equals(command.getStatus())) {
            throw new Exception("This command is already being executed");
        }
        if (StatusEnum.DRY_RUN_EXECUTION_START.getName().equals(command.getStatus())) {
            throw new Exception("This command is already being executed in dry run");
        }
    }
}
