package icu.ydg.controller;

import icu.ydg.annotation.Verify;
import icu.ydg.common.ApiResponse;
import icu.ydg.common.ApiResult;
import icu.ydg.common.ErrorCode;
import icu.ydg.constant.UserConstant;
import icu.ydg.model.domain.User;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.service.PostCommentPraiseService;
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
 * 帖子评论点赞接口
 *
 * @author 袁德光
 * @Date 2024/11/03
 */
@RestController
@RequestMapping("/postCommentPraise")
@Slf4j
//@Api(tags = "帖子评论点赞模块接口")
public class PostCommentPraiseController {

    @Resource
    private PostCommentPraiseService postCommentPraiseService;

    @Resource
    private UserService userService;

    /**
     * 点赞 / 取消点赞
     *
     * @param postPraiseAddRequest 帖子点赞添加请求
     * @param request              请求
     * @return resultNum 本次点赞变化数
     */
    @PostMapping("/")
    @ApiOperation(value = "点赞/取消点赞（需要 user 权限）")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Integer> doPraise(@RequestBody IdRequest postPraiseAddRequest,
                                        HttpServletRequest request) {
        ThrowUtils.throwIf(postPraiseAddRequest == null || postPraiseAddRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 登录才能点赞
        final User loginUser = userService.getLoginUser(request);
        long postId = postPraiseAddRequest.getId();
        int result = postCommentPraiseService.doPostCommentPraise(postId, loginUser);
        return ApiResult.success(result);
    }
}
