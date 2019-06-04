package rcwd.helper;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.exec.LogOutputStream;
import rcwd.service.TelegramService;

public class ProcessingLogOutputStream extends LogOutputStream {

    private TelegramService telegramHelper;
    private String task;
    private CircularFifoQueue<String> lastLogLines;
    private boolean printRcloneToConsole;
    private MessageHelper messageHelper;

    public ProcessingLogOutputStream(TelegramService telegramHelper, String task, CircularFifoQueue<String> lastLogLines, int logLinesToReport) {
        this(telegramHelper, task, lastLogLines, logLinesToReport, false);
    }

    public ProcessingLogOutputStream(TelegramService telegramHelper, String task, CircularFifoQueue<String> lastLogLines, int logLinesToReport, Boolean printRcloneToConsole) {
        this.telegramHelper = telegramHelper;
        this.task = task;
        this.lastLogLines = lastLogLines;
        this.printRcloneToConsole = printRcloneToConsole;
        messageHelper = new MessageHelper(logLinesToReport);
    }

    @Override
    protected void processLine(String line, int level) {
        if (line.contains("ERROR")) {
            if(telegramHelper!=null){
                telegramHelper.sendTelegramMessage(messageHelper.buildErrorText(task, line));
            }
        }
        lastLogLines.add(line);

        if (printRcloneToConsole) {
            System.out.println(line);
        }
    }
}
