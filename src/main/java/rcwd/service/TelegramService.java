package rcwd.service;

import okhttp3.*;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rcwd.properties.RcwdProperties;
import rcwd.properties.TelegramProperties;

import java.io.IOException;
import java.net.URISyntaxException;

@Service
public class TelegramService {

    @Autowired
    private RcwdProperties properties;

    @Autowired
    private TelegramProperties telegramProperties;

    private final String TELEGRAM_SEND_MESSAGE = "/sendMessage";

    /**
     * Send a message to the telegram bot.
     *
     * @param text message to be sent
     * @return telegram API response
     */
    public String sendTelegramMessage(String text) {
        if(Boolean.TRUE.equals(properties.getSendTelegramMessages())){
            try {
                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, "{\"text\":\""+text.replaceAll("\"", "'")+"\"}");

                URIBuilder b = null;
                b = new URIBuilder(telegramProperties.getTelegramApiBase() + telegramProperties.getBotToken() + TELEGRAM_SEND_MESSAGE);
                b.addParameter("chat_id", telegramProperties.getChatId());
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


}
