package rcwd.helper;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.exec.LogOutputStream;
import rcwd.service.TelegramService;

@Log4j2
public class ProcessingLogOutputStream extends LogOutputStream {

    private final TelegramService telegramHelper;
    private final String task;
    private final CircularFifoQueue<String> logQueue;
    private final boolean printRcloneToConsole;
    private final MessageHelper messageHelper;

    public ProcessingLogOutputStream(String task, CircularFifoQueue<String> logQueue, Boolean printRcloneToConsole) {
        this(null, task, logQueue, 0, printRcloneToConsole);
    }

    public ProcessingLogOutputStream(TelegramService telegramHelper, String task, CircularFifoQueue<String> logQueue, int logLinesToReport, Boolean printRcloneToConsole) {
        this.telegramHelper = telegramHelper;
        this.task = task;
        this.logQueue = logQueue;
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
        logQueue.add(line);

        if (printRcloneToConsole) {
            log.debug(line);
        }
    }
}
