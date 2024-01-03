package rcwd.helper;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.exec.LogOutputStream;
import rcwd.model.Command;
import rcwd.service.ExecutionService;
import rcwd.service.TelegramService;

import java.util.concurrent.TimeUnit;

@Log4j2
public class ProcessingLogOutputStream extends LogOutputStream {

    private final TelegramService telegramHelper;
    private final Command task;
    private final CircularFifoQueue<String> logQueue;
    private final boolean printRcloneToConsole;
    private final MessageHelper messageHelper;
    private final ExecutionService executionService;
    /**
     * This is weird usage of the cache class. The purpose is to keep track of how many errors have occurred in a single
     * minute.
     */
    private final Cache<String, String> errorLog = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();
    /**
     * Maximum number of errors that are allowed to occur in a single timespan.
     */
    private final long maxErrorsInTime = 3;
    private final boolean ignoreDiscardedClosedSshConnection;

    public ProcessingLogOutputStream(ExecutionService executionService, Command task, CircularFifoQueue<String> logQueue, Boolean printRcloneToConsole, boolean ignoreDiscardedClosedSshConnection) {
        this(executionService, null, task, logQueue, 0, printRcloneToConsole, ignoreDiscardedClosedSshConnection);
    }

    public ProcessingLogOutputStream(ExecutionService executionService, TelegramService telegramHelper, Command task, CircularFifoQueue<String> logQueue, int logLinesToReport, Boolean printRcloneToConsole, boolean ignoreDiscardedClosedSshConnection) {
        this.executionService = executionService;
        this.telegramHelper = telegramHelper;
        this.task = task;
        this.logQueue = logQueue;
        this.printRcloneToConsole = printRcloneToConsole;
        this.ignoreDiscardedClosedSshConnection = ignoreDiscardedClosedSshConnection;
        messageHelper = new MessageHelper(logLinesToReport);
    }

    @Override
    protected void processLine(String line, int level) {
        if (isError(line)) {
            if(telegramHelper!=null){
                telegramHelper.sendTelegramMessage(messageHelper.buildErrorText(task.getName(), line));
            }
            errorLog.put(line, line);
            // if there are more than 3 errors in a minute
            errorLog.cleanUp();
            if (errorLog.size() >= maxErrorsInTime) {
                if (telegramHelper != null) {
                    telegramHelper.sendTelegramMessage("Killing command because too many errors occurred.");
                }
                executionService.kill(task.getId());
            }
        }
        logQueue.add(line);

        if (printRcloneToConsole) {
            log.debug(line);
        }
    }

    private boolean isError(String line) {
        boolean lineContainsError = line.contains("ERROR");

        if (lineContainsError && ignoreDiscardedClosedSshConnection && line.contains("Discarding closed SSH connection")) {
            return false;
        } else {
            return lineContainsError;
        }
    }
}
