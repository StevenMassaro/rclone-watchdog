package rcwd.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rcwd.mapper.CommandMapper;
import rcwd.mapper.ProfileMapper;
import rcwd.model.Command;
import rcwd.model.Profile;
import rcwd.service.ExecutionService;

@RestController()
@RequestMapping("/execute")
public class ExecuteEndpoint {

    @Autowired
    private CommandMapper commandMapper;

    @Autowired
    private ExecutionService executionService;

    @GetMapping("/command/{commandId}")
    public String executeByCommandId(@PathVariable long commandId){
        Command command = commandMapper.get(commandId);
        executionService.execute(command);
        return Long.toString(command.getId());
    }
}
