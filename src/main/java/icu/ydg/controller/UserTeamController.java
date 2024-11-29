package icu.ydg.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import icu.ydg.annotation.Verify;
import icu.ydg.common.ApiResponse;
import icu.ydg.common.ApiResult;
import icu.ydg.common.ErrorCode;
import icu.ydg.constant.UserConstant;
import icu.ydg.model.domain.User;
import icu.ydg.model.domain.UserTeam;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.userTeam.UserTeamAddRequest;
import icu.ydg.model.dto.userTeam.UserTeamQueryRequest;
import icu.ydg.model.dto.userTeam.UserTeamUpdateRequest;
import icu.ydg.model.vo.userTeam.UserTeamVO;
import icu.ydg.service.UserService;
import icu.ydg.service.UserTeamService;
import icu.ydg.utils.ThrowUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户队伍接口
 *
 * @author 袁德光
 * @Date 2024/11/03
 */
@RestController
@RequestMapping("/userTeam")
@Slf4j
//@Api(tags = "用户队伍接口")
public class UserTeamController {

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建用户队伍
     *
     * @param userTeamAddRequest
     * @param request
     * @return
     */
    @PostMapping("/join")
    @ApiOperation(value = "用户加入队伍")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> addUserTeam(@RequestBody UserTeamAddRequest userTeamAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userTeamAddRequest == null, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(userTeamService.addUserTeam(userTeamAddRequest, request));
    }

    /**
     * 删除用户队伍
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/exit")
    @ApiOperation(value = "用户退出队伍")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> deleteUserTeam(@RequestBody IdRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(userTeamService.deleteUserTeam(deleteRequest, request), "退出成功！");
    }

    /**
     * 更新用户队伍（仅管理员可用）
     *
     * @param userTeamUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @ApiOperation(value = "用户更新自己在队伍的状态（需要 user 权限）")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> updateUserTeam(@RequestBody UserTeamUpdateRequest userTeamUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userTeamUpdateRequest == null || userTeamUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(userTeamService.updateUserTeam(userTeamUpdateRequest, request), "更新成功！");
    }

    /**
     * 根据 id 获取用户队伍（封装类）
     *
     * @param idRequest
     * @return
     */
    @GetMapping("/get/vo")
    @ApiOperation(value = "根据 id 获取用户队伍（封装类）")
    public ApiResponse<UserTeamVO> getUserTeamVOById(IdRequest idRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(idRequest == null || idRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 获取封装类
        return ApiResult.success(userTeamService.getUserTeamVO(idRequest, request));
    }

    /**
     * 分页获取用户队伍列表（仅管理员可用）
     *
     * @param userTeamQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @ApiOperation(value = "分页获取用户队伍列表（仅管理员可用）")
    @Verify(checkAuth = UserConstant.ADMIN_ROLE)
    public ApiResponse<Page<UserTeam>> listUserTeamByPage(@RequestBody UserTeamQueryRequest userTeamQueryRequest) {
        ThrowUtils.throwIf(userTeamQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(userTeamService.getListPage(userTeamQueryRequest));
    }

    /**
     * 分页获取用户队伍列表（封装类）
     *
     * @param userTeamQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    @ApiOperation(value = "分页获取用户队伍列表（封装类）")
    public ApiResponse<Page<UserTeamVO>> listUserTeamVOByPage(@RequestBody UserTeamQueryRequest userTeamQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userTeamQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(userTeamService.handlePaginationAndValidation(userTeamQueryRequest, request));
    }

    /**
     * 分页获取当前登录用户创建的用户队伍列表
     *
     * @param userTeamQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    @ApiOperation(value = "分页获取当前登录用户创建的用户队伍列表")
    public ApiResponse<Page<UserTeamVO>> listMyUserTeamVOByPage(@RequestBody UserTeamQueryRequest userTeamQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userTeamQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        userTeamQueryRequest.setCreateBy(loginUser.getId());
        return ApiResult.success(userTeamService.handlePaginationAndValidation(userTeamQueryRequest, request));
    }

    // endregion

}
