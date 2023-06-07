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
import rcwd.model.StatusEnum;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class CommandMapperIT {

    @Autowired
    private CommandMapper commandMapper;

    @Autowired
    private StatusMapper statusMapper;

    @Autowired
    private DataSource dataSource;

    @Test
    public void list() throws SQLException {
        List<Command> list = commandMapper.list();
        assertTrue(list.isEmpty());

        dataSource.getConnection().prepareCall("INSERT INTO rclonewatchdog.\"source\" (id, \"name\", directory) VALUES(1, 'unRAID Backups', '/data/disks/Temporary/Backups');").execute();
        dataSource.getConnection().prepareCall("INSERT INTO rclonewatchdog.destination (id, \"name\", remote, directory) VALUES(1, 'Google Drive 2TB Encrypted: AutomatedUnRaid', 'GoogleDrive-Limited-Crypt', 'AutomatedUnRaid');").execute();
        dataSource.getConnection().prepareCall("INSERT INTO rclonewatchdog.\"filter\" (id, \"filter\") VALUES(1, '--exclude=Windows Images/**');").execute();
        dataSource.getConnection().prepareCall("INSERT INTO rclonewatchdog.command (name, command, \"source\", destination, \"filter\", hidden, schedule) VALUES('test', 'sync', 1, 1, 1, false, 'sch');").execute();

        statusMapper.insert(1, StatusEnum.DRY_RUN_EXECUTION_START, null);

        list = commandMapper.list();
        assertEquals(1, list.size());
        assertEquals(StatusEnum.DRY_RUN_EXECUTION_START, list.get(0).getStatus());
        assertNotNull(list.get(0).getStatusChangeDate());
        assertEquals("sch", list.get(0).getSchedule());
    }

}
