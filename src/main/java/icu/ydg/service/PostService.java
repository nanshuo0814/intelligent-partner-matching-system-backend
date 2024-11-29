package icu.ydg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import icu.ydg.model.domain.Post;
import icu.ydg.model.domain.User;
import icu.ydg.model.dto.IdBatchRequest;
import icu.ydg.model.dto.post.PostQueryRequest;
import icu.ydg.model.dto.post.PostUpdateRequest;
import icu.ydg.model.vo.post.PostVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 帖子服务
 *
 * @author 袁德光
 * @date 2024/07/26
 */
public interface PostService extends IService<Post> {

    /**
     * 有效帖子
     *
     * @param post post
     * @param add  添加
     */
    void validPost(Post post, boolean add);

    /**
     * 获取帖子封装视图
     *
     * @param post    post
     * @param request 请求
     * @return {@code PostVO}
     */
    PostVO getPostVO(Post post, HttpServletRequest request);

    /**
     * 获取查询包装器
     *
     * @param postQueryRequest post查询请求
     * @return {@code QueryWrapper<Post>}
     */
    LambdaQueryWrapper<Post> getQueryWrapper(PostQueryRequest postQueryRequest);

    /**
     * 获取post vo页面
     *
     * @param postPage 帖子页面
     * @param user     用户
     * @return {@link Page }<{@link PostVO }>
     */
    Page<PostVO> getPostVoPage(Page<Post> postPage, User user);

    /**
     * 更新帖子
     *
     * @param postUpdateRequest 更新后请求
     * @param request           请求
     * @return long
     */
    long updatePost(PostUpdateRequest postUpdateRequest, HttpServletRequest request);

    void shenHeBatchStatus(IdBatchRequest deleteRequest, HttpServletRequest request);
}
