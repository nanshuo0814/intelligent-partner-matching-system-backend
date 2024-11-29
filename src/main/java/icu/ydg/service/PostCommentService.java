package icu.ydg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import icu.ydg.model.domain.PostComment;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.postComment.PostCommentAddRequest;
import icu.ydg.model.dto.postComment.PostCommentQueryRequest;
import icu.ydg.model.dto.postComment.PostCommentUpdateRequest;
import icu.ydg.model.vo.postComment.PostCommentVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 帖子评论服务
 *
 * @author 袁德光
 * @Date 2024/11/02
 */
public interface PostCommentService extends IService<PostComment> {

    /**
     * 校验数据
     *
     * @param postComment
     * @param add 对创建的数据进行校验
     */
    void validPostComment(PostComment postComment, boolean add);

    /**
     * 获取查询条件
     *
     * @param postCommentQueryRequest
     * @return
     */
    LambdaQueryWrapper<PostComment> getQueryWrapper(PostCommentQueryRequest postCommentQueryRequest);
    
    /**
     * 获取帖子评论封装
     *
     * @param idRequest
     * @param request
     * @return
     */
    PostCommentVO getPostCommentVO(IdRequest idRequest, HttpServletRequest request);

    /**
     * 分页获取帖子评论封装
     *
     * @param postCommentPage
     * @param request
     * @return
     */
    Page<PostCommentVO> getPostCommentVOPage(Page<PostComment> postCommentPage, HttpServletRequest request);

    /**
    * 更新帖子评论
    *
    * @param postCommentUpdateRequest 更新后请求
    * @param request           请求
    * @return long
    */
    long updatePostComment(PostCommentUpdateRequest postCommentUpdateRequest, HttpServletRequest request);

    /**
    * 添加帖子评论
    *
    * @param postCommentAddRequest 帖子评论添加请求
    * @param request               请求
    * @return long
    */
    long addPostComment(PostCommentAddRequest postCommentAddRequest, HttpServletRequest request);

    /**
    * 删除帖子评论
    *
    * @param deleteRequest 删除请求
    * @param request       请求
    * @return int
    */
    long deletePostComment(IdRequest deleteRequest, HttpServletRequest request);

    /**
    * “获取列表” 页
    *
    * @param postCommentQueryRequest
    * @return {@link Page }<{@link PostComment }>
    */
    Page<PostComment> getListPage(PostCommentQueryRequest postCommentQueryRequest);

    /**
    * 处理分页和验证
    *
    * @param postCommentQueryRequest
    * @param request                 请求
    * @return {@link Page }<{@link PostCommentVO }>
    */
    Page<PostCommentVO> handlePaginationAndValidation(PostCommentQueryRequest postCommentQueryRequest, HttpServletRequest request);

    /**
    * 只有我或管理员可以做
    *
    * @param request 请求
    * @param id      id
    */
    void onlyMeOrAdministratorCanDo(HttpServletRequest request, Long id);

    /**
     * 列表页vo子项
     *
     * @param postCommentQueryRequest 发表评论查询请求
     * @param request                 请求
     * @return {@link Page }<{@link PostCommentVO }>
     */
    Page<PostCommentVO> listPageVoChildren(PostCommentQueryRequest postCommentQueryRequest, HttpServletRequest request);
}
