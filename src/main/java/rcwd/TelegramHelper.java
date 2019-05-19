package rcwd;

import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.DefaultHttpRequestFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TelegramHelper {
    private String BOT_TOKEN;
    private String CHAT_ID;
    private String TELEGRAM_API_BASE;
    private final String LINE_SEPARATOR = "\n"; // System.lineSeparator() doesn't work
    private final String TELEGRAM_SEND_MESSAGE = "/sendMessage";

    public TelegramHelper(String BOT_TOKEN, String CHAT_ID, String TELEGRAM_API_BASE) {
        this.BOT_TOKEN = BOT_TOKEN;
        this.CHAT_ID = CHAT_ID;
        this.TELEGRAM_API_BASE = TELEGRAM_API_BASE;
    }

    /**
     * Send a message to the telegram bot.
     *
     * @param text message to be sent
     * @return telegram API response
     */
    String sendTelegramMessage(String text) {
        try {
            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, "{\"text\":\""+text+"\"}");

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
    }

    String buildErrorText(String task, String exceptionText) {
        // replace all slashes with double slash
        return "*Failed " + task + "*" + "```\\n" + exceptionText.replaceAll("\\\\","\\\\\\\\") + "```";
    }

    String buildTelegramExecutionStartText(String task) {
        return "*Starting " + task + "*";
    }

    /**
     * Build the message indicating the result of execution.
     */
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
    String buildTelegramExecutionEndText(String task, long startTime, long endTime) {
        return "*Finished " + task + "*" + LINE_SEPARATOR + "Execution time: " + TimeHelper.elapsedTimeToHumanString(startTime, endTime);
    }
}
