package icu.ydg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import icu.ydg.model.domain.PostPraise;
import icu.ydg.model.domain.User;

/**
 * 帖子点赞服务
 *
 * @author 袁德光
 * @date 2024/07/26
 */
public interface PostPraiseService extends IService<PostPraise> {

    /**
     * 点赞 / 取消点赞
     *
     * @param postId    帖子id
     * @param loginUser 登录用户
     * @return int
     */
    int doPostThumb(long postId, User loginUser);

    /**
     * 帖子点赞（内部服务）
     *
     * @param userId 用户id
     * @param postId 帖子id
     * @return int
     */
    int doPostThumbInner(long userId, long postId);

    boolean getIfUserPraiseByPostId(long postId, User loginUser);

}
