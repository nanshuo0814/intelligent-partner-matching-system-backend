package icu.ydg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import icu.ydg.model.domain.PostCommentPraise;
import icu.ydg.model.domain.User;

/**
 * 帖子评论点赞服务
 *
 * @author 袁德光
 * @Date 2024/11/03
 */
public interface PostCommentPraiseService extends IService<PostCommentPraise> {

    /**
     * 做帖子评论好评
     *
     * @param postId    帖子id
     * @param loginUser 登录用户
     * @return int
     */
    int doPostCommentPraise(long postId, User loginUser);

    /**
     * 做帖子评论表扬内心
     *
     * @param userId 用户id
     * @param postId 帖子id
     * @return int
     */
    int doPostCommentPraiseInner(long userId, long postId);
}
