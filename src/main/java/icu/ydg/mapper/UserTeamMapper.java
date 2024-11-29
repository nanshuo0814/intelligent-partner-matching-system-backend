package icu.ydg.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import icu.ydg.model.domain.UserTeam;
import io.lettuce.core.dynamic.annotation.Param;

/**
* @author dell
* @description 针对表【user_team(用户队伍表)】的数据库操作Mapper
* @createDate 2024-11-03 20:51:57
* @Entity generator.model.domain.UserTeam
*/
public interface UserTeamMapper extends BaseMapper<UserTeam> {

}




