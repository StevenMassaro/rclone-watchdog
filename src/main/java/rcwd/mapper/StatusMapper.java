package rcwd.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import rcwd.model.StatusEnum;

@Mapper
@Repository
public interface StatusMapper {

    void insert(@Param("commandId") long commandid,
                @Param("status") StatusEnum status,
                @Param("description") String description);
}
