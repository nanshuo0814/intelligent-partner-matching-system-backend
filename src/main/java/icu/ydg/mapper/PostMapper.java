package icu.ydg.mapper;

import icu.ydg.model.domain.Post;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.Date;
import java.util.List;

/**
 * 帖子 Mapper
 *
 * @author 袁德光
 * @date 2024/07/26
 */
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 查询帖子列表（包括已被删除的数据）
     *
     * @param minUpdateTime 最小更新时间
     * @return {@code List<Post>}
     */
    List<Post> listPostWithDelete(Date minUpdateTime);

}




