package icu.ydg.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import icu.ydg.annotation.Verify;
import icu.ydg.common.ApiResponse;
import icu.ydg.common.ApiResult;
import icu.ydg.common.ErrorCode;
import icu.ydg.constant.UserConstant;
import icu.ydg.exception.BusinessException;
import icu.ydg.model.domain.Post;
import icu.ydg.model.domain.User;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.post.PostQueryRequest;
import icu.ydg.model.vo.post.PostVO;
import icu.ydg.service.PostCollectService;
import icu.ydg.service.PostService;
import icu.ydg.service.UserService;
import icu.ydg.utils.ThrowUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 帖子收藏接口
 *
 * @author 袁德光
 * @date 2024/03/31
 */
@RestController
@RequestMapping("/postCollect")
@Slf4j
//@Api(tags = "帖子收藏模块接口")
public class PostCollectController {

    @Resource
    private PostCollectService postFavourService;
    @Resource
    private PostService postService;
    @Resource
    private UserService userService;

    /**
     * 收藏 / 取消收藏（需要 user 权限）
     *
     * @param request   请求
     * @param idRequest id请求
     * @return resultNum 收藏变化数
     */
    @PostMapping("/")
    @ApiOperation(value = "收藏/取消收藏（需要 user 权限）")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Integer> doPostFavour(@RequestBody IdRequest idRequest,
                                             HttpServletRequest request) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能操作
        final User loginUser = userService.getLoginUser(request);
        long postId = idRequest.getId();
        int result = postFavourService.doPostFavour(postId, loginUser);
        return ApiResult.success(result);
    }

    /**
     * 获取我收藏的帖子列表（需要 user 权限）
     *
     * @param postQueryRequest 帖子查询请求
     * @param request          请求
     * @return {@code ApiResponse<Page<PostVO>>}
     */
    @PostMapping("/my/list/page")
    @ApiOperation(value = "获取当前登录用户自己收藏的帖子（需要 user 权限）")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Page<PostVO>> listMyFavourPostByPage(@RequestBody PostQueryRequest postQueryRequest,
                                                            HttpServletRequest request) {
        if (postQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "每页最多20条");
        Page<Post> postPage = postFavourService.listFavourPostByPage(new Page<>(current, size),
                postService.getQueryWrapper(postQueryRequest), loginUser.getId());
        return ApiResult.success(postService.getPostVoPage(postPage, loginUser));
    }

    /**
     * 获取用户收藏的帖子列表（需要 user 权限）
     *
     * @param postCollectQueryRequest 帖子收藏查询请求
     * @param request                请求
     * @return {@code ApiResponse<Page<PostVO>>}
     */
    @ApiOperation(value = "获取用户收藏的帖子（需要 user 权限）")
    @PostMapping("/list/page")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Page<PostVO>> listFavourPostByPageAndUserId(@RequestBody PostQueryRequest postCollectQueryRequest,
                                                                   HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (postCollectQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = postCollectQueryRequest.getCurrent();
        long size = postCollectQueryRequest.getPageSize();
        Long userId = postCollectQueryRequest.getCreateBy();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20 || userId == null, ErrorCode.PARAMS_ERROR);
        Page<Post> postPage = postFavourService.listFavourPostByPage(new Page<>(current, size),
                postService.getQueryWrapper(postCollectQueryRequest), userId);
        return ApiResult.success(postService.getPostVoPage(postPage, loginUser));
    }

}
