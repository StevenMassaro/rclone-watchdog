package rcwd;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rcwd.properties.RcwdProperties;

import javax.annotation.PostConstruct;
import java.io.File;

@Component
@Log4j2
public class StartupValidator {
    private final RcwdProperties properties;

    public StartupValidator(RcwdProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void validateRclonePath() throws Exception {
        File rcloneExecutable = new File(properties.getRcloneBasePath());
        if (!rcloneExecutable.exists()) {
            throw new Exception("rclone executable at location " + properties.getRcloneBasePath() + " does not exist");
        }
    }
}