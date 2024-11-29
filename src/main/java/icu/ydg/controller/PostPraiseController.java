package icu.ydg.controller;

import icu.ydg.annotation.Verify;
import icu.ydg.common.ApiResponse;
import icu.ydg.common.ApiResult;
import icu.ydg.common.ErrorCode;
import icu.ydg.constant.UserConstant;
import icu.ydg.exception.BusinessException;
import icu.ydg.model.domain.User;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.service.PostPraiseService;
import icu.ydg.service.UserService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 帖子点赞接口
 *
 * @author 袁德光
 * @date 2024/03/31
 */
@RestController
@RequestMapping("/postPraise")
@Slf4j
//@Api(tags = "帖子点赞模块接口")
public class PostPraiseController {

    @Resource
    private PostPraiseService postPraiseService;
    @Resource
    private UserService userService;

    @PostMapping("/userIfPraise")
    @ApiOperation(value = "获取用户是否点赞了该帖子（需要 user 权限）")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Boolean> getIfUserPraiseByPostId(@RequestBody IdRequest postIdRequest,
            HttpServletRequest request) {
        if (postIdRequest == null || postIdRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "帖子id非法");
        }
        final User loginUser = userService.getLoginUser(request);
        long postId = postIdRequest.getId();
        boolean result = postPraiseService.getIfUserPraiseByPostId(postId, loginUser);
        return ApiResult.success(result);
    }

    /**
     * 点赞 / 取消点赞
     *
     * @param postPraiseAddRequest 帖子点赞添加请求
     * @param request              请求
     * @return resultNum 本次点赞变化数
     */
    @PostMapping("")
    @ApiOperation(value = "点赞/取消点赞（需要 user 权限）")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Integer> doThumb(@RequestBody IdRequest postPraiseAddRequest,
                                        HttpServletRequest request) {
        if (postPraiseAddRequest == null || postPraiseAddRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "帖子id非法");
        }
        // 登录才能点赞
        final User loginUser = userService.getLoginUser(request);
        long postId = postPraiseAddRequest.getId();
        int result = postPraiseService.doPostThumb(postId, loginUser);
        return ApiResult.success(result);
    }

}
