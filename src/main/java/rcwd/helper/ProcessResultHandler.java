package rcwd.helper;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.ExecuteException;
import rcwd.mapper.StatusMapper;
import rcwd.model.Command;
import rcwd.model.StatusEnum;
import rcwd.service.ExecutionService;
import rcwd.service.TelegramService;

@Log4j2
public class ProcessResultHandler extends DefaultExecuteResultHandler {

    private final MessageHelper messageHelper;
    private final TelegramService telegramService;
    private final StatusMapper statusMapper;

    private final Command command;
    private final CircularFifoQueue<String> logQueue;
    private final Long startTime;
    private final boolean isDryRun;

    public ProcessResultHandler(MessageHelper messageHelper, TelegramService telegramService, StatusMapper statusMapper, Command command, CircularFifoQueue<String> logQueue, Long startTime, boolean isDryRun) {
        this.messageHelper = messageHelper;
        this.telegramService = telegramService;
        this.statusMapper = statusMapper;
        this.command = command;
        this.logQueue = logQueue;
        this.startTime = startTime;
        this.isDryRun = isDryRun;
    }

    @Override
    public void onProcessComplete(int exitValue) {
        if (isDryRun) {
            telegramService.sendTelegramMessage(messageHelper.buildTelegramDryRunExecutionEndText(command.getName()));
            statusMapper.insert(command.getId(), StatusEnum.DRY_RUN_EXECUTION_SUCCESS, null);
        } else {
            telegramService.sendTelegramMessage(messageHelper.buildTelegramExecutionEndText(command.getName(), startTime, System.nanoTime(), logQueue));
            statusMapper.insert(command.getId(), StatusEnum.EXECUTION_SUCCESS, null);
            command.sendHealthChecksIoCall(messageHelper, logQueue, false);
        }
        ExecutionService.rcPorts.remove(command.getId());
        super.onProcessComplete(exitValue);
    }

    @Override
    public void onProcessFailed(ExecuteException e) {
        log.error("Process failed", e);
        if (isDryRun) {
            telegramService.sendTelegramMessage(messageHelper.buildTelegramDryRunExecutionEndText(command.getName()));
            statusMapper.insert(command.getId(), StatusEnum.DRY_RUN_EXECUTION_FAIL, null);
        } else {
            telegramService.sendTelegramMessage(messageHelper.buildFailureText(command.getName(), e.toString(), logQueue));
            statusMapper.insert(command.getId(), StatusEnum.EXECUTION_FAIL, null);
            command.sendHealthChecksIoCall(messageHelper, logQueue, true);
        }
        ExecutionService.rcPorts.remove(command.getId());
        super.onProcessFailed(e);
    }
}
