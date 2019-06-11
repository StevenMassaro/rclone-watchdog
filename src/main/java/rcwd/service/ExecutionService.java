package rcwd.service;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rcwd.helper.MessageHelper;
import rcwd.helper.ProcessingLogOutputStream;
import rcwd.model.Command;
import rcwd.properties.RcwdProperties;
import rcwd.properties.TelegramProperties;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExecutionService {

    @Autowired
    private RcwdProperties properties;

    @Autowired
    private TelegramProperties telegramProperties;
    
    @Autowired
    private TelegramService telegramService;

    private Map<Long, CircularFifoQueue<String>> logs = new HashMap<>();

    private MessageHelper messageHelper;

    public void execute(List<Command> commands) {
        for (Command command : commands) {
            execute(command);
        }
    }

    /**
     * Perform a dry run of the execution and write the rclone output to the output stream.
     */
    public void dryRun(Command command, OutputStream outputStream){
        CommandLine cmdLine = command.getCommandLine(properties.getRcloneBasePath().trim());
        cmdLine.addArgument("--dry-run");
        System.out.println(cmdLine.toString());
        DefaultExecutor executor = new DefaultExecutor();
        executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        executor.setStreamHandler(streamHandler);
        try {
            executor.execute(cmdLine);
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    public void execute(Command command){
        long startTime = System.nanoTime();
        System.out.println("Begin executing " + command.getId());
        System.out.println("Executing in " + properties.getCurrentDirectory());
        //verifyRcloneNotAlreadyRunning();

        if(messageHelper == null){
            messageHelper = new MessageHelper(properties.getMaxTelegramLogLines());
        }

        telegramService.sendTelegramMessage(messageHelper.buildTelegramExecutionStartText(command.getName()));

        CircularFifoQueue<String> lastLogLines = getLogQueueForCommand(command.getId());
        CommandLine cmdLine = CommandLine.parse(command.getCommandLine(properties.getRcloneBasePath().trim()) + " --verbose");
        DefaultExecutor executor = new DefaultExecutor();
        executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());
        ProcessingLogOutputStream logOutputStream = new ProcessingLogOutputStream(telegramService, command.getName(), lastLogLines, properties.getMaxTelegramLogLines(), properties.getPrintRcloneToConsole());
        PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(logOutputStream);
        executor.setStreamHandler(pumpStreamHandler);
        try {
            executor.execute(cmdLine);
            telegramService.sendTelegramMessage(messageHelper.buildTelegramExecutionEndText(command.getName(), startTime, System.nanoTime(), lastLogLines));
        } catch (IOException e) {
            telegramService.sendTelegramMessage(messageHelper.buildFailureText(command.getName(), e.toString(), lastLogLines));
            System.out.println(e.toString());
        }

        System.out.println("Finish executing " + command.getId());
    }

    public CircularFifoQueue<String> getLogQueueForCommand(long commandId){
        CircularFifoQueue<String> queue = logs.get(commandId);
        if(queue == null){
            logs.put(commandId, new CircularFifoQueue<String>(properties.getMaxLogLines()));
        }
        return logs.get(commandId);
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
