package rcwd.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rcwd.properties.RcwdProperties;
import rcwd.properties.TelegramProperties;

@Service
@Log4j2
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
            TelegramBot bot = new TelegramBot(telegramProperties.getBotToken());
            SendMessage request = new SendMessage(telegramProperties.getChatId(), text)
                    .parseMode(ParseMode.Markdown);

            SendResponse sendResponse = bot.execute(request);
            return sendResponse.toString();
        } else {
            return null;
        }
    }


}
