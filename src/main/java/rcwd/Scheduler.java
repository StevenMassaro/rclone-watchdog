package rcwd;

import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.support.CronTrigger;
import rcwd.mapper.CommandMapper;
import rcwd.model.Command;
import rcwd.service.ExecutionService;

import java.util.List;

/**
 * This class runs at startup to schedule commands which specify a schedule in their command definition.
 */
@EnableScheduling
@Configuration
@Log4j2
public class Scheduler {

    private final TaskScheduler executor;
    private final CommandMapper commandMapper;
    private final ExecutionService executionService;

    @Autowired
    public Scheduler(TaskScheduler taskExecutor, CommandMapper commandMapper, ExecutionService executionService) {
        this.executor = taskExecutor;
        this.commandMapper = commandMapper;
        this.executionService = executionService;
    }

    @PostConstruct
    public void scheduleCommands() {
        List<Command> commands = commandMapper.list();
        for (Command command : commands) {
            String schedule = command.getSchedule();
            if (StringUtils.isNotEmpty(schedule)) {
                try {
                    executor.schedule(() -> {
                        try {
                            executionService.execute(command, true, false);
                        } catch (Exception e) {
                            log.error("Failed to execute scheduled job for command ID {}", command.getId(), e);
                        }
                    }, new CronTrigger(schedule));
                    log.info("Configured command ID {} to execute following schedule {}", command.getId(), schedule);
                } catch (Exception e) {
                    log.error("Failed to configure schedule for command ID {} with schedule {}", command.getId(), schedule, e);
                }
            } else {
                log.trace("Not attempting to schedule command ID {} because schedule is {}", command.getId(), schedule);
            }

        }
    }
}
