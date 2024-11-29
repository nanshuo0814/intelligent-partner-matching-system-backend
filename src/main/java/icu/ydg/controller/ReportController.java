package icu.ydg.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import icu.ydg.annotation.Verify;
import icu.ydg.common.ApiResponse;
import icu.ydg.common.ApiResult;
import icu.ydg.common.ErrorCode;
import icu.ydg.constant.UserConstant;
import icu.ydg.model.domain.Report;
import icu.ydg.model.domain.User;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.report.ReportAddRequest;
import icu.ydg.model.dto.report.ReportQueryRequest;
import icu.ydg.model.dto.report.ReportUpdateRequest;
import icu.ydg.model.vo.report.ReportVO;
import icu.ydg.service.ReportService;
import icu.ydg.service.UserService;
import icu.ydg.utils.ThrowUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 反馈和举报接口
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@RestController
@RequestMapping("/report")
@Slf4j
//@Api(tags = "反馈和举报接口")
public class ReportController {

    @Resource
    private ReportService reportService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建反馈和举报
     *
     * @param reportAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "创建反馈和举报")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> addReport(@RequestBody ReportAddRequest reportAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(reportAddRequest == null, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(reportService.addReport(reportAddRequest, request));
    }

    /**
     * 删除反馈和举报
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @ApiOperation(value = "删除反馈和举报")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> deleteReport(@RequestBody IdRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(reportService.deleteReport(deleteRequest, request), "删除成功！");
    }

    /**
     * 更新反馈和举报（仅管理员可用）
     *
     * @param reportUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @ApiOperation(value = "更新反馈和举报（需要 user 权限）")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> updateReport(@RequestBody ReportUpdateRequest reportUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(reportUpdateRequest == null || reportUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(reportService.updateReport(reportUpdateRequest, request), "更新成功！");
    }

    /**
     * 根据 id 获取反馈和举报（封装类）
     *
     * @param idRequest
     * @return
     */
    @GetMapping("/get/vo")
    @ApiOperation(value = "根据 id 获取反馈和举报（封装类）")
    public ApiResponse<ReportVO> getReportVOById(IdRequest idRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(idRequest == null || idRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 获取封装类
        return ApiResult.success(reportService.getReportVO(idRequest, request));
    }

    /**
     * 分页获取反馈和举报列表（仅管理员可用）
     *
     * @param reportQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @ApiOperation(value = "分页获取反馈和举报列表（仅管理员可用）")
    @Verify(checkAuth = UserConstant.ADMIN_ROLE)
    public ApiResponse<Page<Report>> listReportByPage(@RequestBody ReportQueryRequest reportQueryRequest) {
        ThrowUtils.throwIf(reportQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(reportService.getListPage(reportQueryRequest));
    }

    /**
     * 分页获取反馈和举报列表（封装类）
     *
     * @param reportQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    @ApiOperation(value = "分页获取反馈和举报列表（封装类）")
    public ApiResponse<Page<ReportVO>> listReportVOByPage(@RequestBody ReportQueryRequest reportQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(reportQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(reportService.handlePaginationAndValidation(reportQueryRequest, request));
    }

    /**
     * 分页获取当前登录用户创建的反馈和举报列表
     *
     * @param reportQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    @ApiOperation(value = "分页获取当前登录用户创建的反馈和举报列表")
    public ApiResponse<Page<ReportVO>> listMyReportVOByPage(@RequestBody ReportQueryRequest reportQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(reportQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        reportQueryRequest.setCreateBy(loginUser.getId());
        return ApiResult.success(reportService.handlePaginationAndValidation(reportQueryRequest, request));
    }

    // endregion

}
