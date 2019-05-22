package rcwd.helper;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.exec.LogOutputStream;
import rcwd.service.TelegramService;

import static rcwd.helper.MessageHelper.buildErrorText;

public class ProcessingLogOutputStream extends LogOutputStream {

    private TelegramService telegramHelper;
    private String task;
    private CircularFifoQueue<String> lastLogLines;

    public ProcessingLogOutputStream(TelegramService telegramHelper, String task, CircularFifoQueue<String> lastLogLines) {
        this.telegramHelper = telegramHelper;
        this.task = task;
        this.lastLogLines = lastLogLines;
    }

    @Override
    protected void processLine(String line, int level) {
        if (line.contains("ERROR")) {
            if(telegramHelper!=null){
                telegramHelper.sendTelegramMessage(buildErrorText(task, line));
            }
        }
        lastLogLines.add(line);

        System.out.println(line);
    }
}
