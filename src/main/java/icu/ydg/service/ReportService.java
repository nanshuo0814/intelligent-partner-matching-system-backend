package icu.ydg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import icu.ydg.model.domain.Report;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.report.ReportAddRequest;
import icu.ydg.model.dto.report.ReportQueryRequest;
import icu.ydg.model.dto.report.ReportUpdateRequest;
import icu.ydg.model.vo.report.ReportVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 反馈和举报服务
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
public interface ReportService extends IService<Report> {

    /**
     * 校验数据
     *
     * @param report
     * @param add 对创建的数据进行校验
     */
    void validReport(Report report, boolean add);

    /**
     * 获取查询条件
     *
     * @param reportQueryRequest
     * @return
     */
    LambdaQueryWrapper<Report> getQueryWrapper(ReportQueryRequest reportQueryRequest);
    
    /**
     * 获取反馈和举报封装
     *
     * @param idRequest
     * @param request
     * @return
     */
    ReportVO getReportVO(IdRequest idRequest, HttpServletRequest request);

    /**
     * 分页获取反馈和举报封装
     *
     * @param reportPage
     * @param request
     * @return
     */
    Page<ReportVO> getReportVOPage(Page<Report> reportPage, HttpServletRequest request);

    /**
    * 更新反馈和举报
    *
    * @param reportUpdateRequest 更新后请求
    * @param request           请求
    * @return long
    */
    long updateReport(ReportUpdateRequest reportUpdateRequest, HttpServletRequest request);

    /**
    * 添加反馈和举报
    *
    * @param reportAddRequest 反馈和举报添加请求
    * @param request               请求
    * @return long
    */
    long addReport(ReportAddRequest reportAddRequest, HttpServletRequest request);

    /**
    * 删除反馈和举报
    *
    * @param deleteRequest 删除请求
    * @param request       请求
    * @return int
    */
    long deleteReport(IdRequest deleteRequest, HttpServletRequest request);

    /**
    * “获取列表” 页
    *
    * @param reportQueryRequest
    * @return {@link Page }<{@link Report }>
    */
    Page<Report> getListPage(ReportQueryRequest reportQueryRequest);

    /**
    * 处理分页和验证
    *
    * @param reportQueryRequest
    * @param request                 请求
    * @return {@link Page }<{@link ReportVO }>
    */
    Page<ReportVO> handlePaginationAndValidation(ReportQueryRequest reportQueryRequest, HttpServletRequest request);

    /**
    * 只有我或管理员可以做
    *
    * @param request 请求
    * @param id      id
    */
    void onlyMeOrAdministratorCanDo(HttpServletRequest request, Long id);
}
