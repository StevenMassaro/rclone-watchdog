package rcwd.helper;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.exec.LogOutputStream;
import rcwd.service.TelegramService;

import static rcwd.helper.MessageHelper.buildErrorText;

public class ProcessingLogOutputStream extends LogOutputStream {

    private TelegramService telegramHelper;
    private String task;
    private CircularFifoQueue<String> lastLogLines;
    private boolean printRcloneToConsole;

    public ProcessingLogOutputStream(TelegramService telegramHelper, String task, CircularFifoQueue<String> lastLogLines) {
        this(telegramHelper, task, lastLogLines, false);
    }

    public ProcessingLogOutputStream(TelegramService telegramHelper, String task, CircularFifoQueue<String> lastLogLines, Boolean printRcloneToConsole) {
        this.telegramHelper = telegramHelper;
        this.task = task;
        this.lastLogLines = lastLogLines;
        this.printRcloneToConsole = printRcloneToConsole;
    }

    @Override
    protected void processLine(String line, int level) {
        if (line.contains("ERROR")) {
            if(telegramHelper!=null){
                telegramHelper.sendTelegramMessage(buildErrorText(task, line));
            }
        }
        lastLogLines.add(line);

        if (printRcloneToConsole) {
            System.out.println(line);
        }
    }
}
