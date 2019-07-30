package rcwd.service;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.exec.*;
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

import static org.apache.commons.exec.ExecuteWatchdog.INFINITE_TIMEOUT;

@Service
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
        System.out.println(cmdLine.toString());
        DefaultExecutor executor = new DefaultExecutor();
        executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        executor.setStreamHandler(streamHandler);
        try {
            executor.execute(cmdLine);
            statusMapper.insert(command.getId(), StatusEnum.DRY_RUN_EXECUTION_SUCCESS, null);
        } catch (IOException e) {
            System.out.println(e.toString());
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
        System.out.println("Begin executing " + command.getId());
        statusMapper.insert(command.getId(), StatusEnum.EXECUTION_START, null);
        System.out.println("Executing in " + properties.getCurrentDirectory());
        //verifyRcloneNotAlreadyRunning();

        if(messageHelper == null){
            messageHelper = new MessageHelper(properties.getMaxTelegramLogLines());
        }

        telegramService.sendTelegramMessage(messageHelper.buildTelegramExecutionStartText(command.getName()));

        CircularFifoQueue<String> logQueue = getLogQueueForCommand(command.getId());
        CommandLine cmdLine = command.getCommandLine(properties.getRcloneBasePath().trim());
        cmdLine.addArgument("--verbose");
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
            System.out.println(e.toString());
            statusMapper.insert(command.getId(), StatusEnum.EXECUTION_FAIL, null);
        }

        System.out.println("Finish executing " + command.getId());
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
