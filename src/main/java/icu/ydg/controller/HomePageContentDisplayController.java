package icu.ydg.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import icu.ydg.annotation.Verify;
import icu.ydg.common.ApiResponse;
import icu.ydg.common.ApiResult;
import icu.ydg.common.ErrorCode;
import icu.ydg.constant.UserConstant;
import icu.ydg.model.domain.HomePageContentDisplay;
import icu.ydg.model.domain.User;
import icu.ydg.model.dto.IdBatchRequest;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.homePageContentDisplay.HomePageContentDisplayAddRequest;
import icu.ydg.model.dto.homePageContentDisplay.HomePageContentDisplayQueryRequest;
import icu.ydg.model.dto.homePageContentDisplay.HomePageContentDisplayUpdateRequest;
import icu.ydg.model.vo.homePageContentDisplay.HomePageContentDisplayVO;
import icu.ydg.service.HomePageContentDisplayService;
import icu.ydg.service.UserService;
import icu.ydg.utils.ThrowUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 首页内容展示接口
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@RestController
@RequestMapping("/homePageContentDisplay")
@Slf4j
//@Api(tags = "首页内容展示接口")
public class HomePageContentDisplayController {

    @Resource
    private HomePageContentDisplayService homePageContentDisplayService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建首页内容展示
     *
     * @param homePageContentDisplayAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "创建首页内容展示")
    @Verify(checkAuth = UserConstant.ADMIN_ROLE)
    public ApiResponse<Long> addHomePageContentDisplay(@RequestBody HomePageContentDisplayAddRequest homePageContentDisplayAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(homePageContentDisplayAddRequest == null, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(homePageContentDisplayService.addHomePageContentDisplay(homePageContentDisplayAddRequest, request));
    }


    /**
     * 删除首页内容展示
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @ApiOperation(value = "删除首页内容展示")
    @Verify(checkAuth = UserConstant.ADMIN_ROLE)
    public ApiResponse<Long> deleteHomePageContentDisplay(@RequestBody IdRequest deleteRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(homePageContentDisplayService.deleteHomePageContentDisplay(deleteRequest, request),
                "删除成功！");
    }
    
    /**
     * 批量删除首页内容展示
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete/batch")
    @ApiOperation(value = "批量删除首页内容展示")
    @Verify(checkAuth = UserConstant.ADMIN_ROLE)
    public ApiResponse<String> batchDeleteHomePageContentDisplay(@RequestBody IdBatchRequest deleteRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getIds().isEmpty(), ErrorCode.PARAMS_ERROR);
        // 执行批量删除
        homePageContentDisplayService.batchDeleteHomePageContentDisplay(deleteRequest, request);
        return ApiResult.success("批量删除成功！");
    }

    /**
     * 更新首页内容展示（仅管理员可用）
     *
     * @param homePageContentDisplayUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @ApiOperation(value = "更新首页内容展示（需要 user 权限）")
    @Verify(checkAuth = UserConstant.ADMIN_ROLE)
    public ApiResponse<Long> updateHomePageContentDisplay(@RequestBody HomePageContentDisplayUpdateRequest homePageContentDisplayUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(homePageContentDisplayUpdateRequest == null || homePageContentDisplayUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(homePageContentDisplayService.updateHomePageContentDisplay(homePageContentDisplayUpdateRequest, request), "更新成功！");
    }

    /**
     * 根据 id 获取首页内容展示（封装类）
     *
     * @param idRequest
     * @return
     */
    @GetMapping("/get/vo")
    @ApiOperation(value = "根据 id 获取首页内容展示（封装类）")
    @Verify(checkAuth = UserConstant.ADMIN_ROLE)
    public ApiResponse<HomePageContentDisplayVO> getHomePageContentDisplayVOById(IdRequest idRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(idRequest == null || idRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 获取封装类
        return ApiResult.success(homePageContentDisplayService.getHomePageContentDisplayVO(idRequest, request));
    }

    /**
     * 分页获取首页内容展示列表（仅管理员可用）
     *
     * @param homePageContentDisplayQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @ApiOperation(value = "分页获取首页内容展示列表（仅管理员可用）")
    @Verify(checkAuth = UserConstant.ADMIN_ROLE)
    public ApiResponse<Page<HomePageContentDisplay>> listHomePageContentDisplayByPage(@RequestBody HomePageContentDisplayQueryRequest homePageContentDisplayQueryRequest) {
        ThrowUtils.throwIf(homePageContentDisplayQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(homePageContentDisplayService.getListPage(homePageContentDisplayQueryRequest));
    }

    /**
     * 分页获取首页内容展示列表（封装类）
     *
     * @param homePageContentDisplayQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    @ApiOperation(value = "分页获取首页内容展示列表（封装类）")
    @Verify(checkAuth = UserConstant.ADMIN_ROLE)
    public ApiResponse<Page<HomePageContentDisplayVO>> listHomePageContentDisplayVOByPage(@RequestBody HomePageContentDisplayQueryRequest homePageContentDisplayQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(homePageContentDisplayQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(homePageContentDisplayService.handlePaginationAndValidation(homePageContentDisplayQueryRequest, request));
    }

    /**
     * 分页获取当前登录用户创建的首页内容展示列表
     *
     * @param homePageContentDisplayQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    @ApiOperation(value = "分页获取当前登录用户创建的首页内容展示列表")
    @Verify(checkAuth = UserConstant.ADMIN_ROLE)
    public ApiResponse<Page<HomePageContentDisplayVO>> listMyHomePageContentDisplayVOByPage(@RequestBody HomePageContentDisplayQueryRequest homePageContentDisplayQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(homePageContentDisplayQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        homePageContentDisplayQueryRequest.setCreateBy(loginUser.getId());
        return ApiResult.success(homePageContentDisplayService.handlePaginationAndValidation(homePageContentDisplayQueryRequest, request));
    }

    // endregion

    /**
     * 主页显示内容（展示一个通告和若干张轮播图）
     *
     * @return {@link ApiResponse }<{@link Long }>
     */
    @GetMapping("")
    // @Verify(checkAuth = UserConstant.USER_ROLE)
    @ApiOperation(value = "首页展示内容")
    public ApiResponse<Map<String,Object>> homePageDisplayContent() {
        return ApiResult.success(homePageContentDisplayService.homePageContentDisplay());
    }

}
