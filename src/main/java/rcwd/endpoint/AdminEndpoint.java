package rcwd.endpoint;

import org.springframework.web.bind.annotation.*;
import rcwd.service.ExecutionService;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/admin")
public class AdminEndpoint {

    private final ExecutionService executionService;

    public AdminEndpoint(ExecutionService executionService) {
        this.executionService = executionService;
    }

    @PostMapping("/bandwidthLimit/{limit}")
    public int setBandwidthLimit(@PathVariable String limit,
                                 @RequestParam(required = false) final Long secondsToWaitBeforeResettingToDefault) throws Exception {
        return executionService.setBandwidthLimit(limit, secondsToWaitBeforeResettingToDefault);
    }
}
