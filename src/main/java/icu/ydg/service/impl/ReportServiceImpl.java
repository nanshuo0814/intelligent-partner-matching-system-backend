package icu.ydg.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.ydg.common.ErrorCode;
import icu.ydg.constant.PageConstant;
import icu.ydg.constant.UserConstant;
import icu.ydg.exception.BusinessException;
import icu.ydg.mapper.ReportMapper;
import icu.ydg.model.domain.Report;
import icu.ydg.model.domain.User;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.report.ReportAddRequest;
import icu.ydg.model.dto.report.ReportQueryRequest;
import icu.ydg.model.dto.report.ReportUpdateRequest;
import icu.ydg.model.enums.report.ReportStatusEnums;
import icu.ydg.model.enums.report.ReportTypeEnums;
import icu.ydg.model.enums.sort.ReportSortFieldEnums;
import icu.ydg.model.vo.report.ReportVO;
import icu.ydg.model.vo.user.UserVO;
import icu.ydg.service.PostCommentService;
import icu.ydg.service.PostService;
import icu.ydg.service.ReportService;
import icu.ydg.service.TeamService;
import icu.ydg.service.UserService;
import icu.ydg.utils.SqlUtils;
import icu.ydg.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 反馈和举报服务实现
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@Service
@Slf4j
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    @Resource
    private UserService userService;
    @Resource
    private ReportMapper reportMapper;
    @Resource
    private PostService postService;
    @Resource
    private PostCommentService postCommentService;
    @Resource
    private TeamService teamService;

    /**
     * 添加反馈和举报
     *
     * @param reportAddRequest 发表评论添加请求
     * @param request          请求
     * @return long
     */
    @Override
    public long addReport(ReportAddRequest reportAddRequest, HttpServletRequest request) {
        Report report = new Report();
        BeanUtils.copyProperties(reportAddRequest, report);
        // 数据校验
        validReport(report, true);
        // 写入数据库
        int insert = reportMapper.insert(report);
        ThrowUtils.throwIf(insert <= 0, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newReportId = report.getId();
        // 返回新写入的数据 id
        return report.getId();
    }

    /**
     * 删除反馈和举报
     *
     * @param deleteRequest 删除请求
     * @param request       请求
     * @return long
     */
    @Override
    public long deleteReport(IdRequest deleteRequest, HttpServletRequest request) {
        long id = deleteRequest.getId();
        onlyMeOrAdministratorCanDo(request, id);
        ThrowUtils.throwIf(reportMapper.deleteById(id) <= 0, ErrorCode.OPERATION_ERROR);
        return id;
    }

    /**
     * 校验数据
     *
     * @param report
     * @param add    对创建的数据进行校验
     */
    @Override
    public void validReport(Report report, boolean add) {
        ThrowUtils.throwIf(report == null, ErrorCode.PARAMS_ERROR);
        if (!add) {
            Long id = report.getId();
            ThrowUtils.throwIf(id == null || id <= 0, "id 非法");
            Integer status = report.getStatus();
            ThrowUtils.throwIf(ReportStatusEnums.getEnumByValue(status) == null, "status 非法");
        }
        String content = report.getContent();
        ThrowUtils.throwIf(StringUtils.isBlank(content), "内容不能为空");
        Integer type = report.getType();
        ThrowUtils.throwIf(ReportTypeEnums.getEnumByValue(type) == null, "type 非法");

        // 第一种方法：创建一个映射表来映射type到对应的服务和异常信息
        Map<Integer, Function<Long, Boolean>> validationMap = new HashMap<>();
        validationMap.put(0, reportId -> userService.getById(reportId) != null);
        validationMap.put(1, reportId -> postService.getById(reportId) != null);
        validationMap.put(2, reportId -> postCommentService.getById(reportId) != null);
        //validationMap.put(3, reportId -> chatService.getById(reportId) != null);
        validationMap.put(4, reportId -> teamService.getById(reportId) != null);
        Long reportId = report.getReportId();
        ThrowUtils.throwIf(reportId == null || reportId <= 0, "reportId 非法");
        // 获取对应的验证方法
        Function<Long, Boolean> validate = validationMap.get(type);
        // 如果没有找到对应的验证方法，说明type非法
        if (validate == null) {
            throw new BusinessException("非法的 type: " + type);
        }
        // 执行验证
        ThrowUtils.throwIf(!validate.apply(reportId), "举报的资源不存在");

        // 第二种方法：
        //if (type == 0) {
        //    ThrowUtils.throwIf(userService.getById(reportId) == null, "举报的用户不存在");
        //} else if (type == 1) {
        //    ThrowUtils.throwIf(postService.getById(reportId) == null, "举报的评论不存在");
        //} else if (type == 2) {
        //    ThrowUtils.throwIf(postCommentService.getById(reportId) == null, "举报的帖子不存在");
        //} else if (type == 3) {
        //    throw new BusinessException("当前还未实现聊天举报！");
        //    //ThrowUtils.throwIf(chatService.getById(reportId) == null, "举报的回复不存在");
        //} else {
        //    throw new BusinessException("type 非法");
        //}
    }

    /**
     * 获取查询条件
     *
     * @param reportQueryRequest
     * @return
     */
    @Override
    public LambdaQueryWrapper<Report> getQueryWrapper(ReportQueryRequest reportQueryRequest) {
        LambdaQueryWrapper<Report> queryWrapper = new LambdaQueryWrapper<>();
        if (reportQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = reportQueryRequest.getId();
        Long notId = reportQueryRequest.getNotId();
        String content = reportQueryRequest.getContent();
        String searchText = reportQueryRequest.getSearchText();
        String sortField = reportQueryRequest.getSortField();
        String sortOrder = reportQueryRequest.getSortOrder();
        Long userId = reportQueryRequest.getCreateBy();
        Long reportId = reportQueryRequest.getReportId();
        Integer type = reportQueryRequest.getType();
        // if (type != null) {
        //     ThrowUtils.throwIf(ReportStatusEnums.getEnumByValue(type) == null, "type 类型错误");
        // }
        Integer status = reportQueryRequest.getStatus();
        // if (status != null) {
        //     ThrowUtils.throwIf(ReportStatusEnums.getEnumByValue(status) == null, "status 类型错误");
        // }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(content), Report::getContent, content);
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), Report::getId, notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), Report::getId, id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(reportId), Report::getReportId, reportId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(type), Report::getType, type);
        queryWrapper.eq(ObjectUtils.isNotEmpty(status), Report::getStatus, status);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), Report::getCreateBy, userId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(PageConstant.SORT_ORDER_ASC), isSortField(sortField));
        return queryWrapper;
    }

    /**
     * 是否为排序字段
     *
     * @param sortField 排序字段
     * @return {@code SFunction<Report, ?>}
     */
    private SFunction<Report, ?> isSortField(String sortField) {
        if (Objects.equals(sortField, "")) {
            sortField = ReportSortFieldEnums.UPDATE_TIME.name();
        }
        if (SqlUtils.validSortField(sortField)) {
            return ReportSortFieldEnums.fromString(sortField).map(ReportSortFieldEnums::getFieldGetter).orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "排序字段非法"));
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "排序字段无效，未知错误");
        }
    }

    /**
     * 获取反馈和举报封装
     *
     * @param idRequest
     * @param request
     * @return
     */
    @Override
    public ReportVO getReportVO(IdRequest idRequest, HttpServletRequest request) {
        long id = idRequest.getId();
        // 查询数据库
        Report report = reportMapper.selectById(id);
        ThrowUtils.throwIf(report == null, ErrorCode.NOT_FOUND_ERROR);
        // 对象转封装类
        ReportVO reportVO = ReportVO.objToVo(report);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = report.getCreateBy();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        reportVO.setUser(userVO);
        return reportVO;
    }

    /**
     * “获取列表” 页
     *
     * @param reportQueryRequest
     * @return {@link Page }<{@link Report }>
     */
    @Override
    public Page<Report> getListPage(ReportQueryRequest reportQueryRequest) {
        long current = reportQueryRequest.getCurrent();
        long size = reportQueryRequest.getPageSize();
        if (size == 0L) {
            size = PageConstant.PAGE_SIZE;
        }
        if (current == 0L) {
            current = PageConstant.CURRENT_PAGE;
        }
        // 查询数据库
        return this.page(new Page<>(current, size), this.getQueryWrapper(reportQueryRequest));
    }

    /**
     * 分页获取反馈和举报封装
     *
     * @param reportPage
     * @param request
     * @return
     */
    @Override
    public Page<ReportVO> getReportVOPage(Page<Report> reportPage, HttpServletRequest request) {
        List<Report> reportList = reportPage.getRecords();
        Page<ReportVO> reportVOPage = new Page<>(reportPage.getCurrent(), reportPage.getSize(), reportPage.getTotal());
        if (CollUtil.isEmpty(reportList)) {
            return reportVOPage;
        }
        // 对象列表 => 封装对象列表
        List<ReportVO> reportVOList = reportList.stream().map(report -> {
            return ReportVO.objToVo(report);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = reportList.stream().map(Report::getCreateBy).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        //Map<Long, Boolean> reportIdHasPraiseMap = new HashMap<>();
        //Map<Long, Boolean> reportIdHasCollectMap = new HashMap<>();
        //User loginUser = userService.getLoginUserPermitNull(request);
        //if (loginUser != null) {
        //    Set<Long> reportIdSet = reportList.stream().map(Report::getId).collect(Collectors.toSet());
        //    loginUser = userService.getLoginUser(request);
        //    // 获取点赞
        //    LambdaQueryWrapper<ReportPraise> reportPraiseQueryWrapper = new LambdaQueryWrapper<>();
        //    reportPraiseQueryWrapper.in(ReportPraise::getId, reportIdSet);
        //    reportPraiseQueryWrapper.eq(ReportPraise::getCreateBy, loginUser.getId());
        //    List<ReportPraise> reportReportPraiseList = reportThumbMapper.selectList(reportThumbQueryWrapper);
        //    reportReportThumbList.forEach(reportReportPraise -> reportIdHasPraiseMap.put(reportReportPraise.getReportId(), true));
        //    // 获取收藏
        //    LambdaQueryWrapper<ReportCollect> reportCollectQueryWrapper = new LambdaQueryWrapper<>();
        //    reportCollectQueryWrapper.in(ReportCollect::getId, reportIdSet);
        //    reportCollectQueryWrapper.eq(ReportCollect::getCreateBy, loginUser.getId());
        //    List<ReportCollect> reportCollectList = reportCollectMapper.selectList(reportCollectQueryWrapper);
        //    reportCollectList.forEach(reportCollect -> reportIdHasCollectMap.put(reportCollect.getReportId(), true));
        //}
        // 填充信息
        reportVOList.forEach(reportVO -> {
            Long userId = reportVO.getCreateBy();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            reportVO.setUser(userService.getUserVO(user));
        });
        // endregion

        reportVOPage.setRecords(reportVOList);
        return reportVOPage;
    }

    /**
     * 更新反馈和举报
     *
     * @param reportUpdateRequest 更新后请求
     * @param request             请求
     * @return long
     */
    @Override
    public long updateReport(ReportUpdateRequest reportUpdateRequest, HttpServletRequest request) {
        // 本人和管理员可修改
        User user = userService.getLoginUser(request);
        if (!UserConstant.ADMIN_ROLE.equals(user.getUserRole()) && !Objects.equals(user.getId(), reportUpdateRequest.getCreateBy())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        long id = reportUpdateRequest.getId();
        // 获取数据
        Report oldReport = reportMapper.selectById(id);
        ThrowUtils.throwIf(oldReport == null, ErrorCode.NOT_FOUND_ERROR);
        Integer status = reportUpdateRequest.getStatus();
        oldReport.setStatus(status);
        oldReport.setContent(reportUpdateRequest.getContent());
        // 参数校验
        validReport(oldReport, false);
        // 更新
        reportMapper.updateById(oldReport);
        return id;
    }

    /**
     * 处理分页和验证
     *
     * @param reportQueryRequest 反馈和举报查询请求
     * @param request            请求
     * @return {@code Page<ReportVO>}
     */
    @Override
    public Page<ReportVO> handlePaginationAndValidation(ReportQueryRequest reportQueryRequest, HttpServletRequest request) {
        long current = reportQueryRequest.getCurrent();
        long size = reportQueryRequest.getPageSize();
        if (size == 0L) {
            size = PageConstant.PAGE_SIZE;
        }
        if (current == 0L) {
            current = PageConstant.CURRENT_PAGE;
        }
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Report> reportPage = this.page(new Page<>(current, size), this.getQueryWrapper(reportQueryRequest));
        return this.getReportVOPage(reportPage, request);
    }

    /**
     * 只有本人或管理员可以执行
     *
     * @param request 请求
     * @param id      id
     */
    @Override
    public void onlyMeOrAdministratorCanDo(HttpServletRequest request, Long id) {
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        Report oldReport = reportMapper.selectById(id);
        ThrowUtils.throwIf(oldReport == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可操作（这里假设"编辑"和"删除"操作的权限是一样的）
        if (!oldReport.getCreateBy().equals(loginUser.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "你当前暂无该权限！");
        }
    }

}
