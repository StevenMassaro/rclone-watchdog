package rcwd;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import rcwd.mapper.CommandMapper;
import rcwd.mapper.StatusMapper;
import rcwd.model.Command;
import rcwd.model.StatusEnum;

import javax.annotation.PostConstruct;
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

    public FailedJobStartupUpdater(CommandMapper commandMapper, StatusMapper statusMapper) {
        this.commandMapper = commandMapper;
        this.statusMapper = statusMapper;
    }

    @PostConstruct
    public void markJobsAsFailed() {
        List<Command> commands = commandMapper.list();
        if (commands != null) {
            for (Command command : commands) {
                if (StatusEnum.DRY_RUN_EXECUTION_START.equals(command.getStatus()) || StatusEnum.EXECUTION_START.equals(command.getStatus())) {
                    log.warn("Marking command {} as failed", command.getId());
                    StatusEnum newStatus = StatusEnum.DRY_RUN_EXECUTION_START.equals(command.getStatus()) ? StatusEnum.DRY_RUN_EXECUTION_FAIL : StatusEnum.EXECUTION_FAIL;
                    statusMapper.insert(command.getId(), newStatus, "Automatically marking failed on startup");
                }
            }
        }
    }
}
