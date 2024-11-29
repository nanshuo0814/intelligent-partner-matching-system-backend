package icu.ydg.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import icu.ydg.model.domain.Post;
import icu.ydg.model.domain.PostCollect;
import org.apache.ibatis.annotations.Param;

/**
 * 帖子收藏 Mapper
 *
 * @author 袁德光
 * @date 2024/07/26
 */
public interface PostCollectMapper extends BaseMapper<PostCollect> {

    /**
     * 分页查询收藏帖子列表
     *
     * @param page         第页
     * @param queryWrapper 查询包装器
     * @param favourUserId 支持用户id
     * @return {@code Page<Post>}
     */
    Page<Post> listFavourPostByPage(IPage<Post> page, @Param(Constants.WRAPPER) Wrapper<Post> queryWrapper, long favourUserId);

}




