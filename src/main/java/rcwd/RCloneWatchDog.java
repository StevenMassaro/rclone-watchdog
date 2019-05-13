package rcwd;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public class RCloneWatchDog {

    private static final String BOT_TOKEN = "";
    private static final long CHAT_ID = 0L;
    private static final String TELEGRAM_API_BASE = "https://api.telegram.org/bot";
    private static final String TELEGRAM_SEND_MESSAGE = "/sendMessage";

    public static void main(String[] args) throws IOException, URISyntaxException {
        String line = "rclone.exe sync \"C:\\test\" google:test -v";
        CommandLine cmdLine = CommandLine.parse(line);
        DefaultExecutor executor = new DefaultExecutor();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        executor.setStreamHandler(streamHandler);
        executor.execute(cmdLine);
        System.out.println(outputStream.toString());

        URIBuilder b = new URIBuilder(TELEGRAM_API_BASE + BOT_TOKEN + TELEGRAM_SEND_MESSAGE);
        b.addParameter("chat_id", Long.toString(CHAT_ID));
        b.addParameter("text", outputStream.toString());
        b.addParameter("parse_mode", "Markdown");

        String telegramResponse = IOUtils.toString(b.build(), StandardCharsets.UTF_8);
        System.out.println(telegramResponse);
    }
}
