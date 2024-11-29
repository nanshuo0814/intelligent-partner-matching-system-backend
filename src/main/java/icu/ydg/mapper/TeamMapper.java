package icu.ydg.mapper;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import icu.ydg.model.domain.Team;
import icu.ydg.model.vo.team.TeamVO;

/**
* @author dell
* @description 针对表【team(队伍表)】的数据库操作Mapper
* @createDate 2024-11-03 13:45:56
* @Entity generator.model.domain.Team
*/
public interface TeamMapper extends BaseMapper<Team> {
}




