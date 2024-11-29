package icu.ydg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.ydg.common.ErrorCode;
import icu.ydg.exception.BusinessException;
import icu.ydg.mapper.PostPraiseMapper;
import icu.ydg.model.domain.Message;
import icu.ydg.model.domain.Post;
import icu.ydg.model.domain.PostPraise;
import icu.ydg.model.domain.User;
import icu.ydg.model.enums.message.MessageTypeEnums;
import icu.ydg.service.MessageService;
import icu.ydg.service.PostPraiseService;
import icu.ydg.service.PostService;
import icu.ydg.utils.SpringBeanContextUtils;
import icu.ydg.utils.redis.RedisUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 帖子点赞服务实现类
 *
 * @author 袁德光
 * @date 2024/07/26
 */
@Service
public class PostPraiseServiceImpl extends ServiceImpl<PostPraiseMapper, PostPraise>
        implements PostPraiseService {

    @Resource
    private PostService postService;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    @Lazy
    private MessageService messageService;

    @Override
    public boolean getIfUserPraiseByPostId(long postId, User loginUser) {
        LambdaQueryWrapper<PostPraise> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PostPraise::getPostId, postId);
        queryWrapper.eq(PostPraise::getCreateBy, loginUser.getId());
        PostPraise postPraise = this.getOne(queryWrapper);
        if (postPraise != null) {
            return true;
        }
        return false;
    } 

    /**
     * 点赞 / 取消点赞
     *
     * @param postId    帖子id
     * @param loginUser 登录用户
     * @return int
     */
    @Override
    public int doPostThumb(long postId, User loginUser) {
        // 判断实体是否存在，根据类别获取实体
        Post post = postService.getById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "帖子不存在或已删除！");
        }
        // 是否已点赞
        long userId = loginUser.getId();
        // 每个用户串行点赞
        // 锁必须要包裹住事务方法
        PostPraiseService postPraiseService = SpringBeanContextUtils.getBeanByClass(PostPraiseService.class);
        synchronized (String.valueOf(userId).intern()) {
            return postPraiseService.doPostThumbInner(userId, postId);
        }
    }

    /**
     * 点赞 / 取消点赞
     * 封装了事务的方法
     *
     * @param userId 用户id
     * @param postId 帖子id
     * @return int
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int doPostThumbInner(long userId, long postId) {
        PostPraise postThumb = new PostPraise();
        postThumb.setCreateBy(userId);
        postThumb.setUpdateBy(userId);
        postThumb.setPostId(postId);
        QueryWrapper<PostPraise> postThumbQueryWrapper = new QueryWrapper<>(postThumb);
        PostPraise oldPostThumb = this.getOne(postThumbQueryWrapper);
        boolean result;
        int thumbNumChange = 0; // 用于记录点赞数的变化，1表示增加，-1表示减少
        // 已点赞，执行取消点赞操作
        if (oldPostThumb != null) {
            result = this.remove(postThumbQueryWrapper);
            thumbNumChange = -1; // 取消点赞，点赞数减少
        } else {
            // 未点赞，执行点赞操作
            result = this.save(postThumb);
            thumbNumChange = 1; // 点赞，点赞数增加
        }
        if (result) {
            // 更新帖子点赞数
            Post post = postService.getById(postId);
            if (post != null) {
                // 使用Lambda表达式更新帖子点赞数
                boolean updateResult = postService.lambdaUpdate()
                        .eq(Post::getId, postId)
                        .set(Post::getPraiseNum, post.getPraiseNum() + thumbNumChange) // 根据thumbNumChange的值增加或减少点赞数
                        .update();
                // 更新Redis中的点赞数，判断是否有key，若没有设置为1，有则加1
                //redisUtils.incr(RedisKeyConstant.MESSAGE_POST_PRAISE_NUM_KEY + post.getCreateBy(), 1);
                Message message = new Message();
                message.setType(MessageTypeEnums.POST_PRAISE.getValue());
                message.setCreateBy(userId);
                message.setToId(post.getCreateBy());
                message.setContent(String.valueOf(post.getId()));
                messageService.save(message);
                return updateResult ? thumbNumChange : 0; // 返回变化的值，1或-1
            } else {
                // 如果当前点赞数不足以执行减少操作，则抛出异常或进行其他处理
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "帖子不存在或已删除！");
            }
        } else {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

}