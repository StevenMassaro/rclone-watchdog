package rcwd.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import rcwd.model.Status;
import rcwd.model.StatusEnum;

import java.util.List;

@Mapper
@Repository
public interface StatusMapper {

    void insert(@Param("commandId") long commandid,
                @Param("status") StatusEnum status,
                @Param("description") String description);

    List<Status> listExecutions(long commandId);
}
