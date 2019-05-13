package rcwd;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RCloneWatchDog {

    private static final String BOT_TOKEN = "";
    private static final long CHAT_ID = 0L;
    private static final String TELEGRAM_API_BASE = "https://api.telegram.org/bot";
    private static final String TELEGRAM_SEND_MESSAGE = "/sendMessage";
    private static final String LINE_SEPARATOR = "\n"; // System.lineSeparator() doesn't work
    private static final String CURRENT_DIRECTORY = System.getProperty("user.dir");
    private static final String RCLONE_COMMANDS_FILENAME = "rclone_commands.cmd";

    public static void main(String[] args) {
        System.out.println("Executing in " + CURRENT_DIRECTORY);

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
            b.addParameter("chat_id", Long.toString(CHAT_ID));
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
