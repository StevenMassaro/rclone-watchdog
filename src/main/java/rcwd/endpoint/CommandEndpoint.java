package rcwd.endpoint;

import org.springframework.web.bind.annotation.*;
import rcwd.mapper.CommandMapper;
import rcwd.model.Command;
import rcwd.properties.RcwdProperties;
import rcwd.service.ExecutionService;

import java.util.ArrayList;
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
    public String execute(@PathVariable long commandId, @RequestParam(required = false, defaultValue = "false") boolean force) throws Exception {
        Command command = commandMapper.get(commandId);
        executionService.execute(command, true, force);
        return Long.toString(command.getId());
    }

    @GetMapping("/{commandId}/dryrun")
    public void dryRun(@PathVariable long commandId, @RequestParam(required = false, defaultValue = "false") boolean force) throws Exception {
        Command command = commandMapper.get(commandId);
        executionService.dryRun(command, force);
    }

    @GetMapping("/{commandId}/log")
    public List<String> getLog(@PathVariable long commandId){
        return new ArrayList<>(executionService.getLogQueueForCommand(commandId, false));
    }

    @GetMapping("/{commandId}/kill")
    public void kill(@PathVariable long commandId){
        executionService.kill(commandId);
    }
}
