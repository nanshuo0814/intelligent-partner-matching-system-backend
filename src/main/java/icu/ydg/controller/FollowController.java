package icu.ydg.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import icu.ydg.annotation.Verify;
import icu.ydg.common.ApiResponse;
import icu.ydg.common.ApiResult;
import icu.ydg.common.ErrorCode;
import icu.ydg.constant.RedisKeyConstant;
import icu.ydg.constant.UserConstant;
import icu.ydg.model.domain.Follow;
import icu.ydg.model.domain.User;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.follow.FollowAddRequest;
import icu.ydg.model.dto.follow.FollowQueryRequest;
import icu.ydg.model.dto.follow.FollowUpdateRequest;
import icu.ydg.model.vo.follow.FollowVO;
import icu.ydg.service.FollowService;
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
 * 关注接口
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@RestController
@RequestMapping("/follow")
@Slf4j
//@Api(tags = "关注接口")
public class FollowController {

    @Resource
    private FollowService followService;

    @Resource
    private UserService userService;
    @Autowired
    private RedisUtils redisUtils;

    // region 增删改查

    /**
     * 创建关注
     *
     * @param followAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "创建关注")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> addFollow(@RequestBody FollowAddRequest followAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(followAddRequest == null, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(followService.addFollow(followAddRequest, request));
    }

    /**
     * 删除关注
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @ApiOperation(value = "删除关注")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> deleteFollow(@RequestBody IdRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(followService.deleteFollow(deleteRequest, request), "删除成功！");
    }

    /**
     * 更新关注（仅管理员可用）
     *
     * @param followUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @ApiOperation(value = "更新关注（需要 user 权限）")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> updateFollow(@RequestBody FollowUpdateRequest followUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(followUpdateRequest == null || followUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(followService.updateFollow(followUpdateRequest, request), "更新成功！");
    }

    /**
     * 根据 id 获取关注（封装类）
     *
     * @param idRequest
     * @return
     */
    @GetMapping("/get/vo")
    @ApiOperation(value = "根据 id 获取关注（封装类）")
    public ApiResponse<FollowVO> getFollowVOById(IdRequest idRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(idRequest == null || idRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 获取封装类
        return ApiResult.success(followService.getFollowVO(idRequest, request));
    }

    /**
     * 分页获取关注列表（仅管理员可用）
     *
     * @param followQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @ApiOperation(value = "分页获取关注列表（仅管理员可用）")
    @Verify(checkAuth = UserConstant.ADMIN_ROLE)
    public ApiResponse<Page<Follow>> listFollowByPage(@RequestBody FollowQueryRequest followQueryRequest) {
        ThrowUtils.throwIf(followQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(followService.getListPage(followQueryRequest));
    }

    /**
     * 分页获取关注列表（封装类）
     *
     * @param followQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    @ApiOperation(value = "分页获取关注列表（封装类）")
    public ApiResponse<Page<FollowVO>> listFollowVOByPage(@RequestBody FollowQueryRequest followQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(followQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(followService.handlePaginationAndValidation(followQueryRequest, request));
    }

    /**
     * 分页获取当前登录用户创建的关注列表
     *
     * @param followQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    @ApiOperation(value = "分页获取当前登录用户创建的关注列表")
    public ApiResponse<Page<FollowVO>> listMyFollowVOByPage(@RequestBody FollowQueryRequest followQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(followQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        followQueryRequest.setCreateBy(loginUser.getId());
        return ApiResult.success(followService.handlePaginationAndValidation(followQueryRequest, request));
    }

    // endregion
    @PostMapping("/list/page/me/vo")
    @ApiOperation(value = "分页获取关注我的伙伴用户")
    public ApiResponse<Page<FollowVO>> listMeFollowVOByPage(@RequestBody FollowQueryRequest followQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(followQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        followQueryRequest.setFollowUserId(loginUser.getId());
        Page<FollowVO> followVOPage = followService.handlePaginationAndValidation(followQueryRequest, request);
        redisUtils.del(RedisKeyConstant.MESSAGE_FANS_NUM_KEY + loginUser.getId());
        return ApiResult.success(followVOPage);
    }

    @PostMapping("/user")
    @ApiOperation(value = "关注用户")
    public ApiResponse<String> followUser(@RequestBody IdRequest idRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(idRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser.getId().equals(idRequest.getId()), ErrorCode.PARAMS_ERROR, "不能自己关注自己");
        return ApiResult.success(followService.followUser(loginUser,idRequest.getId()));
    }

    @PostMapping("/no/user")
    @ApiOperation(value = "取消关注")
    public ApiResponse<String> unfollowUser(@RequestBody IdRequest idRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(idRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        return ApiResult.success(followService.unfollowUser(loginUser,idRequest.getId()));
    }

    @PostMapping("/check/user")
    @ApiOperation(value = "检查是否关注该用户")
    public ApiResponse<Boolean> checkFollowStatus(@RequestBody IdRequest idRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(idRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        return ApiResult.success(followService.checkFollowStatus(loginUser,idRequest.getId()));
    }
}
