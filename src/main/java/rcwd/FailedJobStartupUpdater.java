package rcwd;

import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import rcwd.helper.MessageHelper;
import rcwd.mapper.CommandMapper;
import rcwd.mapper.StatusMapper;
import rcwd.model.Command;
import rcwd.model.StatusEnum;
import rcwd.service.TelegramService;

import java.util.List;

/**
 * On startup, mark all current jobs that are in progress as failed. It is possible that the application was forcibly
 * quit on its last run, which means that the jobs are not running any more (under usual circumstances, like when
 * the docker container restarts or is killed).
 */
@Component
@Log4j2
public class FailedJobStartupUpdater {

    private final CommandMapper commandMapper;
    private final StatusMapper statusMapper;
    private final TelegramService telegramService;
    public static final String failureMessage = "Automatically marking in progress job as failed on startup";

    public FailedJobStartupUpdater(CommandMapper commandMapper, StatusMapper statusMapper, TelegramService telegramService) {
        this.commandMapper = commandMapper;
        this.statusMapper = statusMapper;
        this.telegramService = telegramService;
    }

    @PostConstruct
    public void markJobsAsFailed() {
        List<Command> commands = commandMapper.list();
        if (commands != null) {
            for (Command command : commands) {
                if (StatusEnum.DRY_RUN_EXECUTION_START.equals(command.getStatus()) || StatusEnum.EXECUTION_START.equals(command.getStatus())) {
                    MessageHelper  messageHelper = new MessageHelper(0);
                    log.warn("Marking command {} as failed", command.getId());
                    StatusEnum newStatus = StatusEnum.DRY_RUN_EXECUTION_START.equals(command.getStatus()) ? StatusEnum.DRY_RUN_EXECUTION_FAIL : StatusEnum.EXECUTION_FAIL;
                    statusMapper.insert(command.getId(), newStatus, failureMessage);
                    telegramService.sendTelegramMessage(messageHelper.buildTelegramExecutionFailedOnStartupText(command.getName()));
                }
            }
        }
    }
}
