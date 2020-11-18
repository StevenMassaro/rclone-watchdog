package rcwd.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import rcwd.model.Command;

import java.util.List;

@Mapper
@Repository
public interface CommandMapper {

    List<Command> list();

    Command get(@Param("commandId") long commandId);
}
