package rcwd.endpoint;

import org.springframework.web.bind.annotation.*;
import rcwd.StartupValidator;
import rcwd.model.BandwidthChangeRequest;
import rcwd.properties.RcwdProperties;
import rcwd.service.ExecutionService;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/admin")
public class AdminEndpoint {

    private final ExecutionService executionService;
    private final RcwdProperties rcwdProperties;

    public AdminEndpoint(ExecutionService executionService,
                         RcwdProperties rcwdProperties) {
        this.executionService = executionService;
        this.rcwdProperties = rcwdProperties;
    }

    @PostMapping("/bandwidthLimit/{limit}")
    public int setBandwidthLimit(@PathVariable String limit,
                                 @RequestParam(required = false) final Long minutesToWaitBeforeResettingToDefault,
                                 @RequestBody(required = false) final BandwidthChangeRequest bandwidthChangeRequest) throws Exception {
        if (minutesToWaitBeforeResettingToDefault != null) {
            return executionService.setBandwidthLimit(limit, minutesToWaitBeforeResettingToDefault, TimeUnit.MINUTES);
        } else if (bandwidthChangeRequest != null) {
            return executionService.setBandwidthLimit(limit, (long) bandwidthChangeRequest.getDuration(), bandwidthChangeRequest.getUnit());
        } else {
            return executionService.setBandwidthLimit(limit, null, null);
        }
    }

    @GetMapping("/health")
    public void healthCheck() throws Exception {
        StartupValidator.checkRcloneExecutable(rcwdProperties);
    }
}
