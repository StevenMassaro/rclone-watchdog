package rcwd.endpoint;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import rcwd.helper.ProcessingLogOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static org.apache.commons.io.FileUtils.*;

@RestController
public class TestEndpoint {
    private static final String CURRENT_DIRECTORY = System.getProperty("user.dir");

    @GetMapping("/load")
    public String testload() {
        System.out.println("Executing in " + CURRENT_DIRECTORY);
        long startTime = System.nanoTime();
        //rclone sync "\\TOWER\Temporary\Backups" GoogleDrive-Unlimited-Crypt:AutomatedUnRaid -v
        String rcloneCommand = "endpointtest|/rclone_tasks/rclone sync \"/testsource\" GoogleDrive-Unlimited:unraidtest -v";
//        String rcloneCommand = "endpointtest|/rclone_tasks/rclone config file";
        System.out.println("Begin executing " + rcloneCommand);
        String taskName = rcloneCommand.split("\\|")[0].trim();
        String command = rcloneCommand.split("\\|")[1].trim();

        CircularFifoQueue<String> lastLogLines = new CircularFifoQueue<>(2);
        CommandLine cmdLine = CommandLine.parse(command);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(new File("/rclone_tasks"));
        executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());
        ProcessingLogOutputStream logOutputStream = new ProcessingLogOutputStream(null, taskName, lastLogLines);
        PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(logOutputStream);
        executor.setStreamHandler(pumpStreamHandler);
        try {
            executor.execute(cmdLine);
        } catch (IOException e) {
            System.out.println(e.toString());
        }

        System.out.println("Finish executing " + rcloneCommand);
        return "finish";
    }

    @GetMapping("/showfiles")
    public String showFiles(){
        Collection<File> files = listFiles(new File("/rclone_tasks"), null, true);
        System.out.println(files.size() + "number of files");
        for(File file : files){
            System.out.println(file.getAbsolutePath() + " " + file.getName());
        }
        return "done";
    }
}
