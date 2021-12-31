package rcwd.endpoint;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rcwd.mapper.CommandMapper;
import rcwd.model.Command;
import rcwd.properties.RcwdProperties;
import rcwd.service.ExecutionService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/command")
public class CommandEndpoint {

    private final CommandMapper commandMapper;
    private final ExecutionService executionService;
    private final RcwdProperties properties;

    public CommandEndpoint(CommandMapper commandMapper, ExecutionService executionService, RcwdProperties properties) {
        this.commandMapper = commandMapper;
        this.executionService = executionService;
        this.properties = properties;
    }

    @GetMapping
    public List<Command> list(){
        return commandMapper.list();
    }

    @GetMapping("/{commandId}")
    public Command get(@PathVariable long commandId){
        return commandMapper.get(commandId);
    }

    @GetMapping("/{commandId}/print")
    public String getHumanReadable(@PathVariable long commandId) {
        Command command = commandMapper.get(commandId);
        return command.getCommandLine(properties.getRcloneBasePath().trim()).toString();
    }

    @GetMapping("/{commandId}/execute")
    public String execute(@PathVariable long commandId){
        Command command = commandMapper.get(commandId);
        executionService.execute(command, true);
        return Long.toString(command.getId());
    }

    @GetMapping("/{commandId}/dryrun")
    public void dryRun(@PathVariable long commandId) throws IOException {
        Command command = commandMapper.get(commandId);
        new Thread(() -> executionService.dryRun(command)).start();
    }

    @GetMapping("/{commandId}/log")
    public String getLog(@PathVariable long commandId){
        CircularFifoQueue<String> logs = executionService.getLogQueueForCommand(commandId, false);
        return StringUtils.join(logs, "\n") + "\n";
    }

    @GetMapping("/{commandId}/kill")
    public void kill(@PathVariable long commandId){
        executionService.kill(commandId);
    }
}
