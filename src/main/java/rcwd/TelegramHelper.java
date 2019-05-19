package rcwd;

import okhttp3.*;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class TelegramHelper {
    private String BOT_TOKEN;
    private String CHAT_ID;
    private String TELEGRAM_API_BASE;
    private final String LINE_SEPARATOR = "\n"; // System.lineSeparator() doesn't work
    private final String TELEGRAM_SEND_MESSAGE = "/sendMessage";
    private Boolean SEND_TELEGRAM_MESSAGES;

    public TelegramHelper(String BOT_TOKEN, String CHAT_ID, String TELEGRAM_API_BASE, Boolean SEND_TELEGRAM_MESSAGES) {
        this.BOT_TOKEN = BOT_TOKEN;
        this.CHAT_ID = CHAT_ID;
        this.TELEGRAM_API_BASE = TELEGRAM_API_BASE;
        this.SEND_TELEGRAM_MESSAGES = SEND_TELEGRAM_MESSAGES;
        if(Boolean.FALSE.equals(SEND_TELEGRAM_MESSAGES)){
            System.out.println("WARNING: Not sending Telegram messages.");
        }
    }

    /**
     * Send a message to the telegram bot.
     *
     * @param text message to be sent
     * @return telegram API response
     */
    String sendTelegramMessage(String text) {
        if(Boolean.TRUE.equals(SEND_TELEGRAM_MESSAGES)){
            try {
                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, "{\"text\":\""+text.replaceAll("\"", "'")+"\"}");

                URIBuilder b = null;
                b = new URIBuilder(TELEGRAM_API_BASE + BOT_TOKEN + TELEGRAM_SEND_MESSAGE);
                b.addParameter("chat_id", CHAT_ID);
//            b.addParameter("text", text);
                b.addParameter("parse_mode", "Markdown");
                Request request = new Request.Builder()
                        .url(b.build().toString())
                        .post(body)
                        .addHeader("Content-Type", "application/json")
                        .build();
                Response response = client.newCall(request).execute();
                return response.toString();

            } catch (URISyntaxException e) {
                System.out.println("Failed to build Telegram URI");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Failed to call Telegram API");
                e.printStackTrace();
            }
            return null;
        } else {
            return null;
        }
    }

    String buildErrorText(String task, String exceptionText, CircularFifoQueue<String> lastLogLines) {
        return buildBadTextBase("Error in", task, exceptionText, lastLogLines);
    }

    String buildErrorText(String task, String exceptionText) {
        return buildBadTextBase("Error in", task, exceptionText);
    }

    String buildFailureText(String task, String exceptionText, CircularFifoQueue<String> lastLogLines) {
        return buildBadTextBase("Failed", task, exceptionText, lastLogLines);
    }

    String buildFailureText(String task, String exceptionText) {
        return buildBadTextBase("Failed", task, exceptionText);
    }

    private String buildBadTextBase(String message, String task, String exceptionText, CircularFifoQueue<String> lastLogLines) {
        String text = "*" + message + " " + task + "*" + LINE_SEPARATOR + makeTextCode(exceptionText.replaceAll("\\\\", "\\\\\\\\"));
        if (!lastLogLines.isEmpty()) {
            text += "Log: " + LINE_SEPARATOR + makeTextCode(createStringFromCircularFifoQueue(lastLogLines));
        }
        return text;
    }

    private String buildBadTextBase(String message, String task, String exceptionText) {
        return buildBadTextBase(message, task, exceptionText, new CircularFifoQueue<String>());
    }

    String buildTelegramExecutionStartText(String task) {
        return "*Starting " + task + "*";
    }

    /**
     * Build the message indicating the result of execution.
     */
    @Deprecated
    String buildTelegramExecutionEndText(String task, OutputStream outputStream) {
        String executionResult = outputStream.toString();
        List<String> executionResultLines = Arrays.asList(executionResult.split(LINE_SEPARATOR));
        StringBuilder response = new StringBuilder();
        response.append("*");
        response.append(task);
        response.append(" execution finished.*");
        response.append(LINE_SEPARATOR);
        for (String line : executionResultLines.subList(executionResultLines.size() - 5, executionResultLines.size())) {
            line = line.replaceAll("\t", "");
            line = line.trim().replaceAll(" +", " ");
            response.append(line).append(LINE_SEPARATOR);
        }
        return response.toString().trim();
    }

    /**
     * Build the message indicating the result of execution.
     */
    String buildTelegramExecutionEndText(String task, long startTime, long endTime, CircularFifoQueue<String> logLines) {
        String resultText = "*Finished " + task + "*"
                + LINE_SEPARATOR + "Execution time: " + TimeHelper.elapsedTimeToHumanString(startTime, endTime)
                + LINE_SEPARATOR;

        return resultText + makeTextCode(createStringFromCircularFifoQueue(logLines));
    }

    private String createStringFromCircularFifoQueue(CircularFifoQueue<String> queue) {
        StringBuilder logLineText = new StringBuilder();
        for (String line : queue) {
            logLineText.append(line);
            logLineText.append(LINE_SEPARATOR);
        }
        return logLineText.toString();
    }

    private String makeTextCode(String text) {
        return "```" + LINE_SEPARATOR + text + LINE_SEPARATOR + "```";
    }
}
