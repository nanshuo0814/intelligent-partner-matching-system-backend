package icu.ydg.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import icu.ydg.annotation.Verify;
import icu.ydg.common.ApiResponse;
import icu.ydg.common.ApiResult;
import icu.ydg.common.ErrorCode;
import icu.ydg.constant.PageConstant;
import icu.ydg.constant.UserConstant;
import icu.ydg.exception.BusinessException;
import icu.ydg.model.domain.User;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.user.*;
import icu.ydg.model.vo.user.UserLoginVO;
import icu.ydg.model.vo.user.UserVO;
import icu.ydg.service.UserService;
import icu.ydg.utils.ThrowUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * 用户控制器
 *
 * @author 袁德光
 * @date 2024/07/26
 */
@Slf4j
//@Api(tags = "用户模块接口")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册 Request
     * @return {@code ApiResponse<Long>}
     */
    @PostMapping("/register")
    @ApiOperation(value = "用户注册")
    public ApiResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        return ApiResult.success(userService.userRegister(userRegisterRequest), "注册成功！");
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录Request
     * @return {@code ApiResponse<UserLoginVO>}
     */
    @PostMapping("/login")
    @ApiOperation(value = "用户登录")
    public ApiResponse<UserLoginVO> userLogin(HttpServletRequest request, @RequestBody UserLoginRequest userLoginRequest) {
        return ApiResult.success(userService.userLogin(request, userLoginRequest), "登录成功！");
    }

    /**
     * 获取当前登录用户
     *
     * @param request 请求
     * @return {@code ApiResponse<UserLoginVO>}
     */
    @GetMapping("/get/login")
    @ApiOperation(value = "获取当前登录用户")
    public ApiResponse<UserLoginVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ApiResult.success(userService.getLoginUserVO(user));
    }

    /**
     * 用户注销（需要 user 权限）
     *
     * @param request 请求
     * @return {@code ApiResponse<Boolean>}
     */
    @PostMapping("/logout")
    @ApiOperation(value = "用户注销")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> userLogout(HttpServletRequest request) {
        return ApiResult.success(userService.userLogout(request), "注销成功！");
    }

    /**
     * 用户密码重置(邮箱验证码)
     *
     * @param userPwdResetByEmailRequest 用户密码重置Request
     * @return {@code ApiResponse<Boolean>}
     */
    @PostMapping("/pwd/update/byEmail")
    @ApiOperation(value = "邮箱验证码进行密码修改")
    public ApiResponse<Long> userPasswordUpdateByEmail(HttpServletRequest request, @RequestBody UserPwdUpdateByEmailRequest userPwdResetByEmailRequest) {
        return ApiResult.success(userService.userPasswordUpdateByEmail(request, userPwdResetByEmailRequest), "修改成功！");
    }

    /**
     * 用户自己修改密码(需要 user 权限)
     *
     * @param request                   请求
     * @param userPasswordUpdateRequest 用户密码更新Request
     * @return {@code ApiResponse<Boolean>}
     */
    @PostMapping("/pwd/update")
    @ApiOperation(value = "用户自行修改密码(需要 user 权限)")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> userPasswordUpdateByMyself(HttpServletRequest request, @RequestBody UserPasswordUpdateRequest userPasswordUpdateRequest) {
        return ApiResult.success(userService.userPasswordUpdateByMyself(request, userPasswordUpdateRequest), "修改成功！");
    }

    /**
     * 用户密码重置(需要 admin 权限)
     *
     * @param idRequest id请求
     * @return {@code ApiResponse<Boolean>}
     */
    @PostMapping("/pwd/reset/byAdmin")
    @Verify(checkAuth = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "重置用户密码（需要 admin 权限）")
    public ApiResponse<Long> userPasswordResetByAdmin(@RequestBody IdRequest idRequest) {
        return ApiResult.success(userService.userPasswordResetByAdmin(idRequest.getId()), "用户密码重置成功！");
    }

    // region 增删改查

    /**
     * 添加用户(需要 admin 权限)
     *
     * @param userAddRequest 用户添加Request
     * @return {@code ApiResponse<Long>}
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加用户（需要 admin 权限）")
    @Verify(checkAuth = UserConstant.ADMIN_ROLE)
    public ApiResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        return ApiResult.success(userService.addUser(userAddRequest, userService.getLoginUser(request)), "添加用户成功！");
    }

    /**
     * 删除用户(需要 admin 权限)
     *
     * @param idRequest 删除请求
     * @return {@code ApiResponse<Long>}
     */
    @PostMapping("/delete")
    @ApiOperation(value = "删除用户（需要 admin 权限）")
    @Verify(checkAuth = UserConstant.ADMIN_ROLE)
    public ApiResponse<Long> deleteUser(@RequestBody IdRequest idRequest, HttpServletRequest request) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户id不存在！");
        }
        User user = userService.getLoginUser(request);
        Long userId = idRequest.getId();
        return ApiResult.success(userService.deleteUser(userId, user), "删除成功！");
    }

    /**
     * 修改用户信息(需要 user 权限)
     *
     * @param userUpdateRequest 用户更新Request
     * @return {@code ApiResponse<Long>}
     */
    @PostMapping("/update")
    @ApiOperation(value = "修改用户信息（需要 user 权限）")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> updateUser(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        long result = userService.updateUserInfo(userUpdateRequest, request);
        return ApiResult.success(result, "修改用户信息成功！");
    }

    /**
     * 按id获取用户(需要 admin 权限)
     *
     * @param idRequest id请求
     * @return {@code ApiResponse<User>}
     */
    @GetMapping("/get/vo")
    @ApiOperation(value = "按id获取用户（需要 user 权限）")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<UserVO> getUserById(IdRequest idRequest) {
        if(idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户id不存在！");
        }
        User user = userService.getById(idRequest.getId());
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在或已删除！");
        UserVO userVO = UserVO.objToVo(user);
        return ApiResult.success(userVO);
    }

    /**
     * 获取查询用户列表Page(需要 admin 权限)
     *
     * @param userQueryRequest 用户查询请求
     * @return {@link ApiResponse }<{@link Page }<{@link User }>>
     */
    @PostMapping("/list/page")
    @Verify(checkAuth = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "获取用户分页信息（需要 admin 权限）")
    public ApiResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        if (size == 0L) {
            size = PageConstant.PAGE_SIZE;
        }
        if (current == 0L) {
            current = PageConstant.CURRENT_PAGE;
        }
        Page<User> userPage = userService.page(new Page<>(current, size), userService.getQueryWrapper(userQueryRequest));
        return ApiResult.success(userPage);
    }

    /**
     * 页面获取用户脱敏vo列表
     *
     * @param userQueryRequest 用户查询Request
     * @return {@code ApiResponse<Page<UserVO>>}
     */
    @PostMapping("/list/page/vo")
    @ApiOperation(value = "获取用户分页视图")
    public ApiResponse<Page<UserVO>> getUserVoListByPage(@RequestBody UserQueryRequest userQueryRequest) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        if (size == 0L) {
            size = PageConstant.PAGE_SIZE;
        }
        if (current == 0L) {
            current = PageConstant.CURRENT_PAGE;
        }
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.OPERATION_ERROR,"一次性获取数量太多啦~");
        Page<User> userPage = userService.page(new Page<>(current, size), userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVoPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVoPage.setRecords(userVOList);
        return ApiResult.success(userVoPage);
    }

    // endregion


    /**
     * 通过标签搜索用户
     * @param tags 要搜索的标签列表
     * @return 匹配的用户列表
     */
    @GetMapping("/searchByTags")
    @ApiOperation(value = "通过标签搜索用户")
    public ApiResponse<List<UserVO>> searchUsersByTags(@RequestParam List<String> tags) {
        ThrowUtils.throwIf(tags.isEmpty(), ErrorCode.OPERATION_ERROR,"请输入标签");
        List<UserVO> users = userService.findUsersByTags(tags);
        return ApiResult.success(users);
    }

    /**
     * 智能算法根据用户的标签获取最匹配的用户
     *
     * @param request
     * @return
     */
    @GetMapping("/match")
    @ApiOperation(value = "智能算法根据用户的标签获取最匹配的用户")
    public ApiResponse<List<UserVO>> matchUsers(HttpServletRequest request) {
        // todo 限制查询用户数量，我这里写死了用户数量，可行扩展，添加参数自定义
        //if (num <= 0 || num > 10) {
        //    throw new BusinessException(ErrorCode.PARAMS_ERROR);
        //}
        User user = userService.getLoginUser(request);
        return ApiResult.success(userService.matchUsers(8, user));
    }

    /**
     * 普通推荐用户
     *
     * @param request 请求
     * @return {@link ApiResponse }<{@link List }<{@link UserVO }>>
     */
    @GetMapping("/recommend")
    @ApiOperation(value = "普通推荐用户")
    public ApiResponse<List<UserVO>> recommendUsers(HttpServletRequest request) {
        // todo 限制查询用户数量，我这里写死了用户数量，可行扩展，添加参数自定义
        //if (num <= 0 || num > 10) {
        //    throw new BusinessException(ErrorCode.PARAMS_ERROR);
        //}
        // 调用业务层方法获取推荐用户
        List<UserVO> recommendedUsers = userService.recommendUsers(8);
        return ApiResult.success(recommendedUsers);
    }

    @GetMapping("/growth")
    @ApiOperation(value = "获取用户增长趋势")
    public ApiResponse<List<UserGrowthRequest>> getUserGrowth() {
        List<UserGrowthRequest> userGrowthData = userService.getUserGrowth();
        return ApiResult.success(userGrowthData);
    }


}