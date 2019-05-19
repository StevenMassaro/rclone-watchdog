package rcwd;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.exec.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RCloneWatchDog {

    private static String BOT_TOKEN;
    private static String CHAT_ID;
    private static String TELEGRAM_API_BASE;
    private static final String CURRENT_DIRECTORY = System.getProperty("user.dir");
    private static String RCLONE_COMMANDS_FILENAME;
    private static Boolean PERFORM_MULTIPLE_RCLONE_EXECUTION_CHECK;
    private static Long CONCURRENT_RCLONE_EXECUTION_LIMIT;
    private static Boolean SEND_TELEGRAM_MESSAGES;
    private static Integer MAX_TELEGRAM_LOG_LINES;
    private static final String SETTINGS_FILENAME = "settings.properties";
    private static TelegramHelper telegramHelper;

    public static void main(String[] args) throws Exception {
        System.out.println("Executing in " + CURRENT_DIRECTORY);
        loadProperties();
        verifyRcloneNotAlreadyRunning();
        telegramHelper = new TelegramHelper(BOT_TOKEN, CHAT_ID, TELEGRAM_API_BASE, SEND_TELEGRAM_MESSAGES);

        List<String> rcloneCommands = new ArrayList<>();
        try {
            rcloneCommands = readRcloneCommands();
        } catch (IOException e) {
            telegramHelper.sendTelegramMessage(telegramHelper.buildFailureText("loading commands", e.toString()));
            System.out.println(e.toString());
        }
        for (String rcloneCommand : rcloneCommands) {
            long startTime = System.nanoTime();
            System.out.println("Begin executing " + rcloneCommand);
            String taskName = rcloneCommand.split("\\|")[0].trim();
            String command = rcloneCommand.split("\\|")[1].trim();

            telegramHelper.sendTelegramMessage(telegramHelper.buildTelegramExecutionStartText(taskName));

            CircularFifoQueue<String> lastLogLines = new CircularFifoQueue<>(MAX_TELEGRAM_LOG_LINES);
            CommandLine cmdLine = CommandLine.parse(command);
            DefaultExecutor executor = new DefaultExecutor();
            executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());
            ProcessingLogOutputStream logOutputStream = new ProcessingLogOutputStream(telegramHelper, taskName, lastLogLines);
            PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(logOutputStream);
            executor.setStreamHandler(pumpStreamHandler);
            try {
                executor.execute(cmdLine);
                telegramHelper.sendTelegramMessage(telegramHelper.buildTelegramExecutionEndText(taskName, startTime, System.nanoTime(), lastLogLines));
            } catch (IOException e) {
                telegramHelper.sendTelegramMessage(telegramHelper.buildFailureText(taskName, e.toString(), lastLogLines));
                System.out.println(e.toString());
            }

            System.out.println("Finish executing " + rcloneCommand);
        }
    }

    private static void verifyRcloneNotAlreadyRunning() throws Exception {
        if (Boolean.TRUE.equals(PERFORM_MULTIPLE_RCLONE_EXECUTION_CHECK)) {
            Runtime rt = Runtime.getRuntime();
            Process p = rt.exec("tasklist.exe");
            String executions = IOUtils.toString(p.getInputStream(), StandardCharsets.UTF_8);

            if (StringUtils.countMatches(executions, "rclone.exe") > CONCURRENT_RCLONE_EXECUTION_LIMIT) {
                throw new Exception(CONCURRENT_RCLONE_EXECUTION_LIMIT + " concurrent " +
                        " " + (CONCURRENT_RCLONE_EXECUTION_LIMIT > 1 ? "instances" : "instance") + " of rclone.exe allowed to be running.");
            }
        }
    }

    private static void loadProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(FileUtils.openInputStream(new File(CURRENT_DIRECTORY + File.separator + SETTINGS_FILENAME)));
        BOT_TOKEN = properties.getProperty("BOT_TOKEN");
        CHAT_ID = properties.getProperty("CHAT_ID");
        TELEGRAM_API_BASE = properties.getProperty("TELEGRAM_API_BASE");
        RCLONE_COMMANDS_FILENAME = properties.getProperty("RCLONE_COMMANDS_FILENAME");
        PERFORM_MULTIPLE_RCLONE_EXECUTION_CHECK = Boolean.valueOf(properties.getProperty("PERFORM_MULTIPLE_RCLONE_EXECUTION_CHECK"));
        CONCURRENT_RCLONE_EXECUTION_LIMIT = properties.getProperty("CONCURRENT_RCLONE_EXECUTION_LIMIT") == null ? null : Long.valueOf(properties.getProperty("CONCURRENT_RCLONE_EXECUTION_LIMIT"));
        SEND_TELEGRAM_MESSAGES = Boolean.valueOf(properties.getProperty("SEND_TELEGRAM_MESSAGES"));
        MAX_TELEGRAM_LOG_LINES = properties.getProperty("MAX_TELEGRAM_LOG_LINES") == null ? null : Integer.valueOf(properties.getProperty("MAX_TELEGRAM_LOG_LINES"));
    }

    private static List<String> readRcloneCommands() throws IOException {
        return IOUtils.readLines(new FileInputStream(CURRENT_DIRECTORY + File.separator + RCLONE_COMMANDS_FILENAME), StandardCharsets.UTF_8);
    }

}
