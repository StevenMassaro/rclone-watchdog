package rcwd.endpoint;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rcwd.service.ExecutionService;

@RestController
@RequestMapping("/admin")
public class AdminEndpoint {

    private final ExecutionService executionService;

    public AdminEndpoint(ExecutionService executionService) {
        this.executionService = executionService;
    }

    @PostMapping("/bandwidthLimit/{limit}")
    public int setBandwidthLimit(@PathVariable String limit) throws Exception {
        return executionService.setBandwidthLimit(limit);
    }
}
