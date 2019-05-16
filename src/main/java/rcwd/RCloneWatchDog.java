package rcwd;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class RCloneWatchDog {

    private static String BOT_TOKEN;
    private static String CHAT_ID;
    private static String TELEGRAM_API_BASE;
    private static final String TELEGRAM_SEND_MESSAGE = "/sendMessage";
    private static final String LINE_SEPARATOR = "\n"; // System.lineSeparator() doesn't work
    private static final String CURRENT_DIRECTORY = System.getProperty("user.dir");
    private static String RCLONE_COMMANDS_FILENAME;
    private static final String SETTINGS_FILENAME = "settings.properties";

    public static void main(String[] args) throws IOException {
        System.out.println("Executing in " + CURRENT_DIRECTORY);
        loadProperties();

        List<String> rcloneCommands = new ArrayList<>();
        try {
            rcloneCommands = readRcloneCommands();
        } catch (IOException e) {
            System.out.println("Failed to read " + RCLONE_COMMANDS_FILENAME);
            e.printStackTrace();
        }
        for (String rcloneCommand : rcloneCommands) {
            String taskName = rcloneCommand.split("\\|")[0].trim();
            String command = rcloneCommand.split("\\|")[1].trim();

            sendTelegramMessage(buildTelegramExecutionStartText(taskName));

            CommandLine cmdLine = CommandLine.parse(command);
            DefaultExecutor executor = new DefaultExecutor();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
            executor.setStreamHandler(streamHandler);
            try {
                executor.execute(cmdLine);
            } catch (IOException e) {
                System.out.println("Failed to execute " + taskName);
                e.printStackTrace();
            }

            sendTelegramMessage(buildTelegramExecutionEndText(taskName, outputStream));
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

    /**
     * Send a message to the telegram bot.
     * @param text message to be sent
     * @return telegram API response
     */
    private static String sendTelegramMessage(String text) {
        try {
            URIBuilder b = null;
            b = new URIBuilder(TELEGRAM_API_BASE + BOT_TOKEN + TELEGRAM_SEND_MESSAGE);
            b.addParameter("chat_id", CHAT_ID);
            b.addParameter("text", text);
            b.addParameter("parse_mode", "Markdown");
            return IOUtils.toString(b.build(), StandardCharsets.UTF_8);
        } catch (URISyntaxException e) {
            System.out.println("Failed to build Telegram URI");;
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Failed to call Telegram API");
            e.printStackTrace();
        }
        return null;
    }

    private static String buildTelegramExecutionStartText(String task) {
        return "*Starting " + task + "*";
    }

    /**
     * Build the message indicating the result of execution.
     */
    static String buildTelegramExecutionEndText(String task, OutputStream outputStream) {
        String executionResult = outputStream.toString();
        List<String> executionResultLines = Arrays.asList(executionResult.split(LINE_SEPARATOR));
        StringBuilder response = new StringBuilder();
        response.append("*");
        response.append(task);
        response.append(" execution finished.*");
        response.append(LINE_SEPARATOR);
        for(String line : executionResultLines.subList(executionResultLines.size() - 5, executionResultLines.size())){
            line = line.replaceAll("\t","");
            line = line.trim().replaceAll(" +", " ");
            response.append(line).append(LINE_SEPARATOR);
        }
        return response.toString().trim();
    }
}
