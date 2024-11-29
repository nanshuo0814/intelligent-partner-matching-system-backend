package icu.ydg.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import icu.ydg.annotation.Verify;
import icu.ydg.common.ApiResponse;
import icu.ydg.common.ApiResult;
import icu.ydg.common.ErrorCode;
import icu.ydg.constant.UserConstant;
import icu.ydg.model.domain.Friend;
import icu.ydg.model.domain.User;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.friend.FriendAddRequest;
import icu.ydg.model.dto.friend.FriendQueryRequest;
import icu.ydg.model.dto.friend.FriendUpdateRequest;
import icu.ydg.model.vo.friend.FriendVO;
import icu.ydg.service.FriendService;
import icu.ydg.service.UserService;
import icu.ydg.utils.ThrowUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 好友接口
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@RestController
@RequestMapping("/friend")
@Slf4j
//@Api(tags = "好友接口")
public class FriendController {

    @Resource
    private FriendService friendService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建好友
     *
     * @param friendAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "创建好友")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> addFriend(@RequestBody FriendAddRequest friendAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(friendAddRequest == null, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(friendService.addFriend(friendAddRequest, request));
    }

    /**
     * 删除好友
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @ApiOperation(value = "删除好友")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> deleteFriend(@RequestBody IdRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(friendService.deleteFriend(deleteRequest, request), "删除成功！");
    }

    /**
     * 更新好友（仅管理员可用）
     *
     * @param friendUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @ApiOperation(value = "更新好友（需要 user 权限）")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> updateFriend(@RequestBody FriendUpdateRequest friendUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(friendUpdateRequest == null || friendUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(friendService.updateFriend(friendUpdateRequest, request), "更新成功！");
    }

    /**
     * 根据 id 获取好友（封装类）
     *
     * @param idRequest
     * @return
     */
    @GetMapping("/get/vo")
    @ApiOperation(value = "根据 id 获取好友（封装类）")
    public ApiResponse<FriendVO> getFriendVOById(IdRequest idRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(idRequest == null || idRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 获取封装类
        return ApiResult.success(friendService.getFriendVO(idRequest, request));
    }

    /**
     * 分页获取好友列表（仅管理员可用）
     *
     * @param friendQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @ApiOperation(value = "分页获取好友列表（仅管理员可用）")
    @Verify(checkAuth = UserConstant.ADMIN_ROLE)
    public ApiResponse<Page<Friend>> listFriendByPage(@RequestBody FriendQueryRequest friendQueryRequest) {
        ThrowUtils.throwIf(friendQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(friendService.getListPage(friendQueryRequest));
    }

    /**
     * 分页获取好友列表（封装类）
     *
     * @param friendQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    @ApiOperation(value = "分页获取好友列表（封装类）")
    public ApiResponse<Page<FriendVO>> listFriendVOByPage(@RequestBody FriendQueryRequest friendQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(friendQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(friendService.handlePaginationAndValidation(friendQueryRequest, request));
    }

    /**
     * 分页获取当前登录用户创建的好友列表
     *
     * @param friendQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    @ApiOperation(value = "分页获取当前登录用户创建的好友列表")
    public ApiResponse<Page<FriendVO>> listMyFriendVOByPage(@RequestBody FriendQueryRequest friendQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(friendQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        friendQueryRequest.setCreateBy(loginUser.getId());
        return ApiResult.success(friendService.handlePaginationAndValidation(friendQueryRequest, request));
    }

    // endregion
}
