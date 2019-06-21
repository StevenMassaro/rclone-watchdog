package rcwd.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import rcwd.model.Command;

import java.util.List;

@Mapper
public interface CommandMapper {

    List<Command> list();

    Command get(@Param("commandId") long commandId);

    void setStatus(@Param("commandId") long commandId, @Param("status") String status);
}
