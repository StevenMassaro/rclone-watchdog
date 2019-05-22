package rcwd.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import rcwd.model.Command;

@Mapper
public interface CommandMapper {

    Command get(@Param("commandId") long commandId);
}
