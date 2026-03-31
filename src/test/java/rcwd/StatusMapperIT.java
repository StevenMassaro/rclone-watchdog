package rcwd;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import rcwd.mapper.CommandMapper;
import rcwd.mapper.StatusMapper;
import rcwd.model.Command;
import rcwd.model.Status;
import rcwd.model.StatusEnum;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class StatusMapperIT {

    @Autowired
    private CommandMapper commandMapper;

    @Autowired
    private StatusMapper statusMapper;

    @Autowired
    private DataSource dataSource;

    @Test
    public void list() throws SQLException {
        dataSource.getConnection().prepareCall("INSERT INTO rclonewatchdog.\"source\" (id, \"name\", directory) VALUES(1, 'unRAID Backups', '/data/disks/Temporary/Backups');").execute();
        dataSource.getConnection().prepareCall("INSERT INTO rclonewatchdog.destination (id, \"name\", remote, directory) VALUES(1, 'Google Drive 2TB Encrypted: AutomatedUnRaid', 'GoogleDrive-Limited-Crypt', 'AutomatedUnRaid');").execute();
        dataSource.getConnection().prepareCall("INSERT INTO rclonewatchdog.\"filter\" (id, \"filter\") VALUES(1, '--exclude=Windows Images/**');").execute();
        dataSource.getConnection().prepareCall("INSERT INTO rclonewatchdog.command (name, command, \"source\", destination, \"filter\", hidden) VALUES('test', 'sync', 1, 1, 1, false);").execute();

        statusMapper.insert(1, StatusEnum.DRY_RUN_EXECUTION_START, "hello world");
        List<Status> statuses = statusMapper.listExecutions(1);
        assertEquals(1, statuses.size());
        assertEquals(1, statuses.get(0).getCommandId());
        assertEquals(3, statuses.get(0).getStatusType());
        assertEquals("hello world", statuses.get(0).getDescription());
        assertNotNull(statuses.get(0).getModifiedDate());
        assertEquals(0, statusMapper.listExecutions(2).size());
    }

}
