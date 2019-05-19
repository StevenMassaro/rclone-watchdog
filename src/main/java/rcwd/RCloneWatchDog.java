package rcwd;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class RCloneWatchDog {

    private static String BOT_TOKEN;
    private static String CHAT_ID;
    private static String TELEGRAM_API_BASE;
    private static final String CURRENT_DIRECTORY = System.getProperty("user.dir");
    private static String RCLONE_COMMANDS_FILENAME;
    private static final String SETTINGS_FILENAME = "settings.properties";
    private static TelegramHelper telegramHelper;

    public static void main(String[] args) throws IOException {
        System.out.println("Executing in " + CURRENT_DIRECTORY);
        loadProperties();
        telegramHelper = new TelegramHelper(BOT_TOKEN, CHAT_ID, TELEGRAM_API_BASE);

        List<String> rcloneCommands = new ArrayList<>();
        try {
            rcloneCommands = readRcloneCommands();
        } catch (IOException e) {
            telegramHelper.sendTelegramMessage(telegramHelper.buildErrorText("loading commands", e.toString()));
            System.out.println(e.toString());
        }
        for (String rcloneCommand : rcloneCommands) {
            long startTime = System.nanoTime();
            System.out.println("Begin executing " + rcloneCommand);
            String taskName = rcloneCommand.split("\\|")[0].trim();
            String command = rcloneCommand.split("\\|")[1].trim();

            telegramHelper.sendTelegramMessage(telegramHelper.buildTelegramExecutionStartText(taskName));

            CommandLine cmdLine = CommandLine.parse(command);
            DefaultExecutor executor = new DefaultExecutor();
            executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());
            try {
                executor.execute(cmdLine);
            } catch (IOException e) {
                telegramHelper.sendTelegramMessage(telegramHelper.buildErrorText(taskName, e.toString()));
                System.out.println(e.toString());
            }

            telegramHelper.sendTelegramMessage(telegramHelper.buildTelegramExecutionEndText(taskName, startTime, System.nanoTime()));
            System.out.println("Finish executing " + rcloneCommand);
        }
    }

    private static void loadProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(FileUtils.openInputStream(new File(CURRENT_DIRECTORY + File.separator + SETTINGS_FILENAME)));
        BOT_TOKEN = properties.getProperty("BOT_TOKEN");
        CHAT_ID = properties.getProperty("CHAT_ID");
        TELEGRAM_API_BASE = properties.getProperty("TELEGRAM_API_BASE");
        RCLONE_COMMANDS_FILENAME = properties.getProperty("RCLONE_COMMANDS_FILENAME");
    }

    private static List<String> readRcloneCommands() throws IOException {
        return IOUtils.readLines(new FileInputStream(CURRENT_DIRECTORY + File.separator + RCLONE_COMMANDS_FILENAME), StandardCharsets.UTF_8);
    }

}
