package rcwd;

import org.apache.commons.exec.LogOutputStream;

public class ProcessingLogOutputStream extends LogOutputStream {

    private TelegramHelper telegramHelper;
    private String task;

    public ProcessingLogOutputStream(TelegramHelper telegramHelper, String task) {
        this.telegramHelper = telegramHelper;
        this.task = task;
    }

    @Override
    protected void processLine(String line, int level) {
        if (line.contains("ERROR")) {
            telegramHelper.sendTelegramMessage(telegramHelper.buildErrorText(task, line));
        }

        System.out.println(line);
    }
}
