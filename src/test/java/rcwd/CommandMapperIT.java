package rcwd;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import rcwd.mapper.CommandMapper;
import rcwd.model.Command;

import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class CommandMapperIT {

    @Autowired
    private CommandMapper commandMapper;

    @Test
    public void list() {
        List<Command> list = commandMapper.list();
        assertTrue(list.isEmpty());
    }

}
