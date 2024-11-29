package icu.ydg.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.ydg.common.ErrorCode;
import icu.ydg.exception.BusinessException;
import icu.ydg.mapper.PostCollectMapper;
import icu.ydg.model.domain.Post;
import icu.ydg.model.domain.PostCollect;
import icu.ydg.model.domain.User;
import icu.ydg.service.PostCollectService;
import icu.ydg.service.PostService;
import icu.ydg.utils.SpringBeanContextUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 帖子收藏服务实现类
 *
 * @author 袁德光
 * @date 2024/07/26
 */
@Service
public class PostCollectServiceImpl extends ServiceImpl<PostCollectMapper, PostCollect>
        implements PostCollectService {

    @Resource
    private PostService postService;

    /**
     * 帖子收藏
     *
     * @param postId    帖子id
     * @param loginUser 登录用户
     * @return int
     */
    @Override
    public int doPostFavour(long postId, User loginUser) {
        // 判断是否存在
        Post post = postService.getById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "帖子不存在");
        }
        // 是否已帖子收藏
        long userId = loginUser.getId();
        // 每个用户串行帖子收藏
        // 锁必须要包裹住事务方法
        PostCollectService postFavourService = SpringBeanContextUtils.getBeanByClass(PostCollectService.class);
        synchronized (String.valueOf(userId).intern()) {
            return postFavourService.doPostFavourInner(userId, postId);
        }
    }

    /**
     * 帖子收藏（内部服务）
     * 封装了事务的方法
     *
     * @param userId 用户id
     * @param postId 帖子id
     * @return int
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int doPostFavourInner(long userId, long postId) {
        PostCollect postFavour = new PostCollect();
        postFavour.setCreateBy(userId);
        postFavour.setUpdateBy(userId);
        postFavour.setPostId(postId);
        QueryWrapper<PostCollect> postFavourQueryWrapper = new QueryWrapper<>(postFavour);
        PostCollect oldPostFavour = this.getOne(postFavourQueryWrapper);
        boolean result;
        int favourNumChange; // 用于记录收藏数的变化，1表示增加，-1表示减少
        // 已收藏，执行取消收藏操作
        if (oldPostFavour != null) {
            result = this.remove(postFavourQueryWrapper);
            favourNumChange = -1; // 取消收藏，收藏数减少
        } else {
            // 未收藏，执行收藏操作
            result = this.save(postFavour);
            favourNumChange = 1; // 收藏，收藏数增加
        }
        if (result) {
            // 更新帖子收藏数
            Post post = postService.getById(postId);
            if (post != null) {
                int newFavourNum = post.getCollectNum() + favourNumChange;
                // 判断收藏数是否为负数
                if (newFavourNum >= 0) {
                    // 使用Lambda表达式更新帖子收藏数
                    boolean updateResult = postService.lambdaUpdate()
                            .eq(Post::getId, postId)
                            .set(Post::getCollectNum, newFavourNum) // 直接设置为新的收藏数
                            .update();
                    return updateResult ? favourNumChange : 0; // 返回变化的值，1或-1
                } else {
                    // 如果收藏数为负数，则抛出异常或进行其他处理
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "收藏数不能为负数");
                }
            } else {
                // 如果未找到帖子，则抛出异常或进行其他处理
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
            }
        } else {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 分页获取用户收藏的帖子列表
     *
     * @param page         第页
     * @param queryWrapper 查询包装器
     * @param favourUserId 收藏用户id
     * @return {@code Page<Post>}
     */
    @Override
    public Page<Post> listFavourPostByPage(IPage<Post> page, Wrapper<Post> queryWrapper, long favourUserId) {
        if (favourUserId <= 0) {
            return new Page<>();
        }
        return baseMapper.listFavourPostByPage(page, queryWrapper, favourUserId);
    }

}




