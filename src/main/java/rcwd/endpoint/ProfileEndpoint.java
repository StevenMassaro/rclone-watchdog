package rcwd.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rcwd.mapper.ProfileMapper;
import rcwd.model.Profile;
import rcwd.service.ExecutionService;

import java.util.List;

@RestController
@RequestMapping("/profile")
public class ProfileEndpoint {

    @Autowired
    private ProfileMapper profileMapper;

    @Autowired
    private ExecutionService executionService;

    @GetMapping
    public List<Profile> list(){
        return profileMapper.list();
    }

    @GetMapping("/{profileId}")
    public Profile get(@PathVariable long profileId) {
        return profileMapper.get(profileId);
    }

    @GetMapping("/{profileId}/executeAndWait")
    public String executeByProfileId(@PathVariable long profileId) {
        Profile profile = profileMapper.get(profileId);
        executionService.execute(profile.getCommands());
        return Long.toString(profile.getId());
    }
}
