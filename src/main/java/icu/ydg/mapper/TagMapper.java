package icu.ydg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import icu.ydg.model.domain.Tag;

import java.util.List;

/**
* @author dell
* @description 针对表【tag(标签表)】的数据库操作Mapper
* @createDate 2024-11-04 10:31:56
* @Entity generator.model.domain.Tag
*/
public interface TagMapper extends BaseMapper<Tag> {

    List<String> findExistingTags(List<String> tagsJson);
}




