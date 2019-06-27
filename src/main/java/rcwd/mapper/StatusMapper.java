package rcwd.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import rcwd.model.StatusEnum;

@Mapper
public interface StatusMapper {

    void insert(@Param("commandId") long commandid,
                @Param("status") StatusEnum status,
                @Param("description") String description);
}
