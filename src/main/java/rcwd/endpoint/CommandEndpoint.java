package rcwd.endpoint;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rcwd.mapper.CommandMapper;
import rcwd.model.Command;
import rcwd.service.ExecutionService;

import java.util.List;

@RestController
@RequestMapping("/command")
public class CommandEndpoint {

    @Autowired
    private CommandMapper commandMapper;

    @Autowired
    private ExecutionService executionService;

    @GetMapping
    public List<Command> list(){
        return commandMapper.list();
    }

    @GetMapping("/{commandId}/execute")
    public String execute(@PathVariable long commandId){
        Command command = commandMapper.get(commandId);
        executionService.execute(command);
        return Long.toString(command.getId());
    }

    @GetMapping("/{commandId}/log")
    public String getLog(@PathVariable long commandId){
        CircularFifoQueue<String> logs = executionService.getLogQueueForCommand(commandId);
        StringBuilder response = new StringBuilder();
        for(String line : logs){
            response.append(line);
            response.append("\n");
        }
        return response.toString();
    }
}