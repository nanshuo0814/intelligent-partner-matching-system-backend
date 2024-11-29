package icu.ydg.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import icu.ydg.annotation.Verify;
import icu.ydg.common.ApiResponse;
import icu.ydg.common.ApiResult;
import icu.ydg.common.ErrorCode;
import icu.ydg.constant.PageConstant;
import icu.ydg.constant.UserConstant;
import icu.ydg.exception.BusinessException;
import icu.ydg.model.domain.Post;
import icu.ydg.model.domain.PostCollect;
import icu.ydg.model.domain.PostComment;
import icu.ydg.model.domain.PostPraise;
import icu.ydg.model.domain.User;
import icu.ydg.model.dto.IdBatchRequest;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.post.PostAddRequest;
import icu.ydg.model.dto.post.PostQueryRequest;
import icu.ydg.model.dto.post.PostUpdateRequest;
import icu.ydg.model.vo.post.PostVO;
import icu.ydg.service.PostCollectService;
import icu.ydg.service.PostCommentService;
import icu.ydg.service.PostPraiseService;
import icu.ydg.service.PostService;
import icu.ydg.service.UserService;
import icu.ydg.utils.ThrowUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 帖子接口
 *
 * @author 袁德光
 * @date 2024/03/31
 */
@RestController
@RequestMapping("/post")
@Slf4j
//@Api(tags = "帖子模块接口")
public class PostController {

    @Resource
    private PostService postService;
    @Resource
    private UserService userService;
    @Resource
    private PostPraiseService postPraiseService;
    @Resource
    private PostCollectService postCollectService;
    @Resource
    private PostCommentService postCommentService;

    // region 增删改查

    /**
     * 添加帖子（需要 user 权限）
     *
     * @param postAddRequest post添加请求
     * @param request        请求
     * @return {@code ApiResponse<Long>}
     */
    @PostMapping("/add")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    @ApiOperation(value = "添加帖子（需要 user 权限）")
    public ApiResponse<Long> addPost(@RequestBody PostAddRequest postAddRequest, HttpServletRequest request) {
        if (postAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        Post post = new Post();
        BeanUtils.copyProperties(postAddRequest, post);
        postService.validPost(post, true);
        User loginUser = userService.getLoginUser(request);
        post.setCreateBy(loginUser.getId());
        post.setCommentNum(0);
        post.setCollectNum(0);
        post.setPraiseNum(0);
        String coverImage = postAddRequest.getCoverImage();
        if (coverImage == null || coverImage.equals("")) {
            post.setCoverImage("https://img1.baidu.com/it/u=3454520222,1120783283&fm=253&fmt=auto&app=120&f=JPEG?w=889&h=500");
        }
        boolean result = postService.save(post);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "添加失败！");
        long newPostId = post.getId();
        return ApiResult.success(newPostId, "添加成功！");
    }

    /**
     * 删除帖子
     *
     * @param idRequest 删除请求
     * @param request   请求
     * @return {@code ApiResponse<Boolean>}
     */
    @PostMapping("/delete")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    @ApiOperation(value = "删除帖子（需要 user 权限）")
    public ApiResponse<Boolean> deletePost(@RequestBody IdRequest idRequest, HttpServletRequest request) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = idRequest.getId();
        onlyMeOrAdministratorCanDo(request, id);
        // 删除帖子点赞和收藏
        LambdaQueryWrapper<PostPraise> postPraiseQueryWrapper = new LambdaQueryWrapper<>();
        postPraiseQueryWrapper.eq(PostPraise::getPostId, id);
        postPraiseService.remove(postPraiseQueryWrapper);
        LambdaQueryWrapper<PostCollect> postCollectQueryWrapper = new LambdaQueryWrapper<>();
        postCollectQueryWrapper.eq(PostCollect::getPostId, id);
        postCollectService.remove(postCollectQueryWrapper);
        // 删除帖子评论
        LambdaQueryWrapper<PostComment> postCommentQueryWrapper = new LambdaQueryWrapper<>();
        postCommentQueryWrapper.eq(PostComment::getPostId, id);
        postCommentService.remove(postCommentQueryWrapper);
        return ApiResult.success(postService.removeById(id), "删除成功！");
    }

    /**
     * 更新（需要 admin 权限）
     *
     * @param postUpdateRequest 更新后请求
     * @return {@code ApiResponse<Boolean>}
     */
    @PostMapping("/update")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    @ApiOperation(value = "更新帖子内容（需要 user 权限）")
    public ApiResponse<Long> updatePost(@RequestBody PostUpdateRequest postUpdateRequest, HttpServletRequest request) {
        if (postUpdateRequest == null || postUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ApiResult.success(postService.updatePost(postUpdateRequest, request), "更新成功！");
    }

    /**
     * 根据 id 获取
     *
     * @param request   请求
     * @param idRequest id请求
     * @return {@code ApiResponse<PostVO>}
     */
    @GetMapping("/get")
    @ApiOperation(value = "根据帖子 id 获取")
    public ApiResponse<PostVO> getPostVoById(IdRequest idRequest, HttpServletRequest request) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = idRequest.getId();
        Post post = postService.getById(id);
        if (post == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "帖子不存在或已删除！");
        }
        return ApiResult.success(postService.getPostVO(post, request));
    }

    // endregion

    // region 分页查询

    /**
     * 分页获取列表（需要 admin 权限）
     *
     * @param postQueryRequest post查询请求
     * @return {@code ApiResponse<Page<Post>>}
     */
    @PostMapping("/list/page")
    @Verify(checkAuth = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "分页获取列表（需要 admin 权限）")
    public ApiResponse<Page<Post>> listPostByPage(@RequestBody PostQueryRequest postQueryRequest, HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        if (!UserConstant.ADMIN_ROLE.equals(user.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限获取帖子列表数据，只有管理员有权限");
        }
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        if (size == 0L) {
            size = PageConstant.PAGE_SIZE;
        }
        if (current == 0L) {
            current = PageConstant.CURRENT_PAGE;
        }
        Page<Post> postPage = postService.page(new Page<>(current, size),
                postService.getQueryWrapper(postQueryRequest));
        return ApiResult.success(postPage);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param postQueryRequest post查询请求
     * @param request          请求
     * @return {@code ApiResponse<Page<PostVO>>}
     */
    @PostMapping("/list/page/vo")
    @ApiOperation(value = "分页获取全部帖子")
    public ApiResponse<Page<PostVO>> listPostVoByPage(@RequestBody PostQueryRequest postQueryRequest,
                                                      HttpServletRequest request) {
        return ApiResult.success(handlePaginationAndValidation(postQueryRequest, request));
    }

    /**
     * 分页获取当前用户创建的帖子列表
     *
     * @param postQueryRequest post查询请求
     * @param request          请求
     * @return {@code ApiResponse<Page<PostVO>>}
     */
    @PostMapping("/list/page/vo/myself")
    @ApiOperation(value = "分页获取当前登录用户创建的帖子")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Page<PostVO>> listMyPostVoByPage(@RequestBody PostQueryRequest postQueryRequest,
                                                        HttpServletRequest request) {
        if (postQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        postQueryRequest.setCreateBy(loginUser.getId());
        return ApiResult.success(handlePaginationAndValidation(postQueryRequest, request));
    }

    /**
     * 处理分页和验证
     *
     * @param postQueryRequest post查询请求
     * @param request          请求
     * @return {@code Page<PostVO>}
     */
    private Page<PostVO> handlePaginationAndValidation(PostQueryRequest postQueryRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        if (size == 0L) {
            size = PageConstant.PAGE_SIZE;
        }
        if (current == 0L) {
            current = PageConstant.CURRENT_PAGE;
        }
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "单页数据量过大，请输入合理的页码");
        Page<Post> postPage = postService.page(new Page<>(current, size), postService.getQueryWrapper(postQueryRequest));
        return postService.getPostVoPage(postPage, loginUser);
    }

    // endregion

    // region 公用方法

    /**
     * 只有本人或管理员可以执行
     *
     * @param request 请求
     * @param id      id
     */
    private void onlyMeOrAdministratorCanDo(HttpServletRequest request, Long id) {
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        Post oldPost = postService.getById(id);
        ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR, "帖子不存在或已删除！");
        // 仅本人或管理员可操作（这里假设"编辑"和"删除"操作的权限是一样的）
        if (!oldPost.getCreateBy().equals(loginUser.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "你当前暂无该权限！");
        }
    }

    // endregion
    @PostMapping("/status/batch")
    @ApiOperation(value = "批量审核帖子状态修改")
    @Verify(checkAuth = UserConstant.ADMIN_ROLE)
    public ApiResponse<String> shenHeBatchStatus(@RequestBody IdBatchRequest deleteRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getIds().isEmpty(), ErrorCode.PARAMS_ERROR);
        // 执行批量删除
        postService.shenHeBatchStatus(deleteRequest, request);
        return ApiResult.success("批量审核成功！");
    }
}
