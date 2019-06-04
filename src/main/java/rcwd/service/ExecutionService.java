package rcwd.service;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rcwd.helper.MessageHelper;
import rcwd.helper.ProcessingLogOutputStream;
import rcwd.model.Command;
import rcwd.properties.RcwdProperties;
import rcwd.properties.TelegramProperties;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static rcwd.helper.MessageHelper.*;

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

    public void execute(Command command){
        long startTime = System.nanoTime();
        System.out.println("Begin executing " + command.getId());
        System.out.println("Executing in " + properties.getCurrentDirectory());
        //verifyRcloneNotAlreadyRunning();

        if(messageHelper == null){
            messageHelper = new MessageHelper(properties.getMaxTelegramLogLines());
        }

        telegramService.sendTelegramMessage(messageHelper.buildTelegramExecutionStartText(command.getName()));

        CircularFifoQueue<String> lastLogLines = new CircularFifoQueue<>(properties.getMaxTelegramLogLines());
        CommandLine cmdLine = CommandLine.parse(properties.getRcloneBasePath().trim());
        cmdLine.addArgument(command.getCommand().trim());
        cmdLine.addArgument(command.getSource().getDirectory().trim());
        cmdLine.addArgument(command.getDestination().getRemote()+ ":" + command.getDestination().getDirectory());
        cmdLine.addArgument(command.getFilters());
        cmdLine.addArgument("--verbose");
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
