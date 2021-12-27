package rcwd.helper;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.ExecuteException;
import rcwd.mapper.StatusMapper;
import rcwd.model.Command;
import rcwd.model.StatusEnum;
import rcwd.service.TelegramService;

@Log4j2
public class ProcessResultHandler extends DefaultExecuteResultHandler {

    private MessageHelper messageHelper;
    private TelegramService telegramService;
    private StatusMapper statusMapper;

    private Command command;
    private CircularFifoQueue<String> logQueue;
    private long startTime;

    public ProcessResultHandler(MessageHelper messageHelper, TelegramService telegramService, StatusMapper statusMapper, Command command, CircularFifoQueue<String> logQueue, long startTime) {
        this.messageHelper = messageHelper;
        this.telegramService = telegramService;
        this.statusMapper = statusMapper;
        this.command = command;
        this.logQueue = logQueue;
        this.startTime = startTime;
    }

    @Override
    public void onProcessComplete(int exitValue) {
        telegramService.sendTelegramMessage(messageHelper.buildTelegramExecutionEndText(command.getName(), startTime, System.nanoTime(), logQueue));
        statusMapper.insert(command.getId(), StatusEnum.EXECUTION_SUCCESS, null);
        super.onProcessComplete(exitValue);
    }

    @Override
    public void onProcessFailed(ExecuteException e) {
        telegramService.sendTelegramMessage(messageHelper.buildFailureText(command.getName(), e.toString(), logQueue));
        log.error("Process failed", e);
        statusMapper.insert(command.getId(), StatusEnum.EXECUTION_FAIL, null);
        super.onProcessFailed(e);
    }
}
