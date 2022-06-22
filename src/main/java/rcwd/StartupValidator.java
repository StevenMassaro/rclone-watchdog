package rcwd;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import rcwd.properties.RcwdProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Arrays;

@Component
@Log4j2
public class StartupValidator {
    private final RcwdProperties properties;
    private final Environment environment;

    public StartupValidator(RcwdProperties properties, Environment environment) {
        this.properties = properties;
        this.environment = environment;
    }

    @PostConstruct
    public void validateRclonePath() throws Exception {
        if (!Arrays.asList(environment.getActiveProfiles()).contains("test")) {
            checkRcloneExecutable(properties);
        }
    }

    public static void checkRcloneExecutable(RcwdProperties properties) throws Exception {
        File rcloneExecutable = new File(properties.getRcloneBasePath());
        if (!rcloneExecutable.exists()) {
            throw new Exception("rclone executable at location " + properties.getRcloneBasePath() + " does not exist");
        }
        if (!rcloneExecutable.canExecute()) {
            throw new Exception("rclone executable at location " + properties.getRcloneBasePath() + " is not executable");
        }
    }
}