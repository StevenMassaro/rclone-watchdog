package rcwd.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import rcwd.model.Profile;

import java.util.List;

@Mapper
public interface ProfileMapper {

    List<Profile> list();

    Profile get(@Param("profileId") long profileId);
}
