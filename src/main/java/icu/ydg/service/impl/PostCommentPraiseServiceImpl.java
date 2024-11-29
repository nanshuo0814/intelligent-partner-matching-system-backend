package icu.ydg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.ydg.common.ErrorCode;
import icu.ydg.exception.BusinessException;
import icu.ydg.mapper.PostCommentPraiseMapper;
import icu.ydg.model.domain.PostComment;
import icu.ydg.model.domain.PostCommentPraise;
import icu.ydg.model.domain.User;
import icu.ydg.service.PostCommentPraiseService;
import icu.ydg.service.PostCommentService;
import icu.ydg.utils.SpringBeanContextUtils;
import icu.ydg.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 帖子评论点赞服务实现
 *
 * @author 袁德光
 * @Date 2024/11/03
 */
@Service
@Slf4j
public class PostCommentPraiseServiceImpl extends ServiceImpl<PostCommentPraiseMapper, PostCommentPraise> implements PostCommentPraiseService {

    @Resource
    private PostCommentService postCommentService;

    @Override
    public int doPostCommentPraise(long postId, User loginUser) {
        // 判断实体是否存在，根据类别获取实体
        PostComment postCommentPraise = postCommentService.getById(postId);
        ThrowUtils.throwIf(postCommentPraise == null, ErrorCode.NOT_FOUND_ERROR);
        // 是否已点赞
        long userId = loginUser.getId();
        // 每个用户串行点赞
        // 锁必须要包裹住事务方法
        PostCommentPraiseService postCommentPraiseService = SpringBeanContextUtils.getBeanByClass(PostCommentPraiseService.class);
        synchronized (String.valueOf(userId).intern()) {
            return postCommentPraiseService.doPostCommentPraiseInner(userId, postId);
        }
    }

    /**
     * 做帖子评论表扬内心
     *
     * @param userId 用户id
     * @param postId 帖子id
     * @return int
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int doPostCommentPraiseInner(long userId, long postId) {
        PostCommentPraise postPraise = new PostCommentPraise();
        postPraise.setCreateBy(userId);
        postPraise.setUpdateBy(userId);
        postPraise.setPostId(postId);
        QueryWrapper<PostCommentPraise> postPraiseQueryWrapper = new QueryWrapper<>(postPraise);
        PostCommentPraise oldPostPraise = this.getOne(postPraiseQueryWrapper);
        boolean result;
        int praiseNumChange = 0; // 用于记录点赞数的变化，1表示增加，-1表示减少
        // 已点赞，执行取消点赞操作
        if (oldPostPraise != null) {
            result = this.remove(postPraiseQueryWrapper);
            praiseNumChange = -1; // 取消点赞，点赞数减少
        } else {
            // 未点赞，执行点赞操作
            result = this.save(postPraise);
            praiseNumChange = 1; // 点赞，点赞数增加
        }
        if (result) {
            // 更新帖子点赞数
            PostComment post = postCommentService.getById(postId);
            if (post != null) {
                // 使用Lambda表达式更新帖子点赞数
                boolean updateResult = postCommentService.lambdaUpdate()
                        .eq(PostComment::getId, postId)
                        .set(PostComment::getPraiseNum, post.getPraiseNum() + praiseNumChange) // 根据praiseNumChange的值增加或减少点赞数
                        .update();
                return updateResult ? praiseNumChange : 0; // 返回变化的值，1或-1
            } else {
                // 如果当前点赞数不足以执行减少操作，则抛出异常或进行其他处理
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "帖子不存在或已删除！");
            }
        } else {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }


}
