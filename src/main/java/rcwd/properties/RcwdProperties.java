package rcwd.properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RcwdProperties {

    private final String currentDirectory = System.getProperty("user.dir");

    @Value("${RCLONE_COMMANDS_FILENAME}")
    private String rcloneCommandsFilename;

    @Value("${PERFORM_MULTIPLE_RCLONE_EXECUTION_CHECK}")
    private Boolean performMultipleRcloneExecutionCheck;

    @Value("${CONCURRENT_RCLONE_EXECUTION_LIMIT}")
    private Long concurrentRcloneExecutionLimit;

    @Value("${MAX_TELEGRAM_LOG_LINES}")
    private Integer maxTelegramLogLines;

    @Value("${SEND_TELEGRAM_MESSAGES}")
    private Boolean sendTelegramMessages;

    @Value("${RCLONE_BASE_PATH}")
    private String rcloneBasePath;

    private final String settingsFilename = "settings.properties";

    public String getCurrentDirectory() {
        return currentDirectory;
    }

    public String getRcloneCommandsFilename() {
        return rcloneCommandsFilename;
    }

    public void setRcloneCommandsFilename(String rcloneCommandsFilename) {
        this.rcloneCommandsFilename = rcloneCommandsFilename;
    }

    public Boolean getPerformMultipleRcloneExecutionCheck() {
        return performMultipleRcloneExecutionCheck;
    }

    public void setPerformMultipleRcloneExecutionCheck(Boolean performMultipleRcloneExecutionCheck) {
        this.performMultipleRcloneExecutionCheck = performMultipleRcloneExecutionCheck;
    }

    public Long getConcurrentRcloneExecutionLimit() {
        return concurrentRcloneExecutionLimit;
    }

    public void setConcurrentRcloneExecutionLimit(Long concurrentRcloneExecutionLimit) {
        this.concurrentRcloneExecutionLimit = concurrentRcloneExecutionLimit;
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

    public String getSettingsFilename() {
        return settingsFilename;
    }

    public String getRcloneBasePath() {
        return rcloneBasePath;
    }

    public void setRcloneBasePath(String rcloneBasePath) {
        this.rcloneBasePath = rcloneBasePath;
    }
}
