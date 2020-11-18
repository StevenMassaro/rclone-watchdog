package rcwd.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import rcwd.model.Profile;

import java.util.List;

@Mapper
@Repository
public interface ProfileMapper {

    List<Profile> list();

    Profile get(@Param("profileId") long profileId);
}
