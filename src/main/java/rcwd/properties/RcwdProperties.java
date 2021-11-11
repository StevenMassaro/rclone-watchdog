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

    // TODO: In the process of making rclone installed in the dockerfile. Need to find a way to pass in the config file.
    @Value("${PRINT_RCLONE_TO_CONSOLE}")
    private Boolean printRcloneToConsole;

    @Value("${MAX_LOG_LINES}")
    private Integer maxLogLines;

    @Value("${RCLONE_BANDWIDTH_SCHEDULE}")
    private String bandwidthSchedule;

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

    public String getBandwidthSchedule() {
        return bandwidthSchedule;
    }
}
