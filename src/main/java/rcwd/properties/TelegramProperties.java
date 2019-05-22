package rcwd.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TelegramProperties {

    @Value("${BOT_TOKEN}")
    private String botToken;

    @Value("${CHAT_ID}")
    private String chatId;

    @Value("${TELEGRAM_API_BASE}")
    private String telegramApiBase;

    public String getBotToken() {
        return botToken;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getTelegramApiBase() {
        return telegramApiBase;
    }

    public void setTelegramApiBase(String telegramApiBase) {
        this.telegramApiBase = telegramApiBase;
    }
}
