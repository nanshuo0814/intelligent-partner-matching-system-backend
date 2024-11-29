package icu.ydg.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import icu.ydg.annotation.Verify;
import icu.ydg.common.ApiResponse;
import icu.ydg.common.ApiResult;
import icu.ydg.common.ErrorCode;
import icu.ydg.constant.RedisKeyConstant;
import icu.ydg.constant.UserConstant;
import icu.ydg.model.domain.PostComment;
import icu.ydg.model.domain.User;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.postComment.PostCommentAddRequest;
import icu.ydg.model.dto.postComment.PostCommentQueryRequest;
import icu.ydg.model.dto.postComment.PostCommentUpdateRequest;
import icu.ydg.model.vo.postComment.PostCommentVO;
import icu.ydg.service.PostCommentService;
import icu.ydg.service.PostService;
import icu.ydg.service.UserService;
import icu.ydg.utils.ThrowUtils;
import icu.ydg.utils.redis.RedisUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 帖子评论接口
 *
 * @author 袁德光
 * @Date 2024/11/02
 */
@RestController
@RequestMapping("/postComment")
@Slf4j
//@Api(tags = "帖子评论模块接口")
public class PostCommentController {

    @Resource
    private PostCommentService postCommentService;
    @Resource
    private PostService postService;

    @Resource
    private UserService userService;
    @Autowired
    private RedisUtils redisUtils;

    // region 增删改查

    /**
     * 创建帖子评论
     *
     * @param postCommentAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "创建帖子评论")
    public ApiResponse<Long> addPostComment(@RequestBody PostCommentAddRequest postCommentAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(postCommentAddRequest == null, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(postCommentService.addPostComment(postCommentAddRequest, request));
    }

    /**
     * 删除帖子评论
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @ApiOperation(value = "删除帖子评论")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> deletePostComment(@RequestBody IdRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(postCommentService.deletePostComment(deleteRequest, request), "删除成功！");
    }

    /**
     * 更新帖子评论
     *
     * @param postCommentUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @ApiOperation(value = "更新帖子评论（需要管理员权限）")
    @Verify(checkAuth = UserConstant.ADMIN_ROLE)
    public ApiResponse<Long> updatePostComment(@RequestBody PostCommentUpdateRequest postCommentUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(postCommentUpdateRequest == null || postCommentUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(postCommentService.updatePostComment(postCommentUpdateRequest, request), "更新成功！");
    }

    /**
     * 根据 id 获取帖子评论（封装类）
     *
     * @param idRequest
     * @return
     */
    @GetMapping("/get/vo")
    @ApiOperation(value = "根据 id 获取帖子评论（封装类）")
    public ApiResponse<PostCommentVO> getPostCommentVOById(IdRequest idRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(idRequest == null || idRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(postCommentService.getPostCommentVO(idRequest, request));
    }

    /**
     * 分页获取帖子评论列表（仅管理员可用）
     *
     * @param postCommentQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @ApiOperation(value = "分页获取帖子评论列表（仅管理员可用）")
    @Verify(checkAuth = UserConstant.ADMIN_ROLE)
    public ApiResponse<Page<PostComment>> listPostCommentByPage(@RequestBody PostCommentQueryRequest postCommentQueryRequest) {
        ThrowUtils.throwIf(postCommentQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(postCommentService.getListPage(postCommentQueryRequest));
    }

    /**
     * 分页获取帖子评论列表（封装类）
     *
     * @param postCommentQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    @ApiOperation(value = "分页获取帖子评论列表（封装类）")
    public ApiResponse<Page<PostCommentVO>> listPostCommentVOByPage(@RequestBody PostCommentQueryRequest postCommentQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(postCommentQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(postCommentService.handlePaginationAndValidation(postCommentQueryRequest, request));
    }

    /**
     * 分页获取当前登录用户创建的帖子评论列表
     *
     * @param postCommentQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    @ApiOperation(value = "分页获取当前登录用户创建的帖子评论列表")
    public ApiResponse<Page<PostCommentVO>> listMyPostCommentVOByPage(@RequestBody PostCommentQueryRequest postCommentQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(postCommentQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        postCommentQueryRequest.setCreateBy(loginUser.getId());
        return ApiResult.success(postCommentService.handlePaginationAndValidation(postCommentQueryRequest, request));
    }

    // endregion

    /**
     * 分页获取一级评论及其子评论
     *
     * @param postCommentQueryRequest 发表评论查询请求
     * @param request                 请求
     * @return {@link ApiResponse }<{@link Page }<{@link PostCommentVO }>>
     */
    @PostMapping("/list/page/vo/children")
    @ApiOperation(value = "分页获取一级评论及其子评论（封装类）")
    public ApiResponse<Page<PostCommentVO>> listPageVoChildren(@RequestBody PostCommentQueryRequest postCommentQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(postCommentQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(postCommentService.listPageVoChildren(postCommentQueryRequest, request));
    }

    /**
     * 列表页评论我
     *
     * @param postCommentQueryRequest 发表评论查询请求
     * @param request                 请求
     * @return {@link ApiResponse }<{@link Page }<{@link PostCommentVO }>>
     */
    @PostMapping("/list/page/comment/me/vo")
    @ApiOperation(value = "分页获取评论我的评论")
    public ApiResponse<Page<PostCommentVO>> listPageCommentMe(@RequestBody PostCommentQueryRequest postCommentQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(postCommentQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        // todo 如果帖子是登录用户的则获取该帖子所有评论信息提醒
        postCommentQueryRequest.setToUserId(loginUser.getId());
        Page<PostCommentVO> postCommentVOPage = postCommentService.handlePaginationAndValidation(postCommentQueryRequest, request);
        redisUtils.del(RedisKeyConstant.MESSAGE_POST_COMMENT_NUM_KEY + loginUser.getId());
        return ApiResult.success(postCommentVOPage);
    }

}
