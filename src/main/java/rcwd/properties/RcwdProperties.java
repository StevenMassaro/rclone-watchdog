package rcwd.properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RcwdProperties {

    private final String currentDirectory = System.getProperty("user.dir");

    @Value("${MAX_TELEGRAM_LOG_LINES}")
    private Integer maxTelegramLogLines;

    @Value("${SEND_TELEGRAM_MESSAGES}")
    private Boolean sendTelegramMessages;

    @Value("${RCLONE_BASE_PATH}")
    private String rcloneBasePath;

    @Value("${PRINT_RCLONE_TO_CONSOLE}")
    private Boolean printRcloneToConsole;

    @Value("${MAX_LOG_LINES}")
    private Integer maxLogLines;

    public String getCurrentDirectory() {
        return currentDirectory;
    }

    public Integer getMaxTelegramLogLines() {
        return maxTelegramLogLines;
    }

    public void setMaxTelegramLogLines(Integer maxTelegramLogLines) {
        this.maxTelegramLogLines = maxTelegramLogLines;
    }

    public Boolean getSendTelegramMessages() {
        return sendTelegramMessages;
    }

    public void setSendTelegramMessages(Boolean sendTelegramMessages) {
        this.sendTelegramMessages = sendTelegramMessages;
    }

    public String getRcloneBasePath() {
        return rcloneBasePath;
    }

    public void setRcloneBasePath(String rcloneBasePath) {
        this.rcloneBasePath = rcloneBasePath;
    }

    public Boolean getPrintRcloneToConsole() {
        return printRcloneToConsole;
    }

    public void setPrintRcloneToConsole(Boolean printRcloneToConsole) {
        this.printRcloneToConsole = printRcloneToConsole;
    }

    public Integer getMaxLogLines() {
        return maxLogLines;
    }

    public void setMaxLogLines(Integer maxLogLines) {
        this.maxLogLines = maxLogLines;
    }
}
