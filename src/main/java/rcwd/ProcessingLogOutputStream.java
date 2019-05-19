package rcwd;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.exec.LogOutputStream;

public class ProcessingLogOutputStream extends LogOutputStream {

    private TelegramHelper telegramHelper;
    private String task;
    private CircularFifoQueue<String> lastLogLines;

    public ProcessingLogOutputStream(TelegramHelper telegramHelper, String task, CircularFifoQueue<String> lastLogLines) {
        this.telegramHelper = telegramHelper;
        this.task = task;
        this.lastLogLines = lastLogLines;
    }

    @Override
    protected void processLine(String line, int level) {
        if (line.contains("ERROR")) {
            telegramHelper.sendTelegramMessage(telegramHelper.buildErrorText(task, line));
        }
        lastLogLines.add(line);

        System.out.println(line);
    }
}
