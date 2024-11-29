package icu.ydg.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.ydg.common.ErrorCode;
import icu.ydg.constant.PageConstant;
import icu.ydg.constant.VerifyParamRegexConstant;
import icu.ydg.exception.BusinessException;
import icu.ydg.mapper.HomePageContentDisplayMapper;
import icu.ydg.model.domain.HomePageContentDisplay;
import icu.ydg.model.domain.User;
import icu.ydg.model.dto.IdBatchRequest;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.homePageContentDisplay.HomePageContentDisplayAddRequest;
import icu.ydg.model.dto.homePageContentDisplay.HomePageContentDisplayQueryRequest;
import icu.ydg.model.dto.homePageContentDisplay.HomePageContentDisplayUpdateRequest;
import icu.ydg.model.enums.homePageContent.HomePageContentStatusEnums;
import icu.ydg.model.enums.sort.HomePageContentDisplaySortFieldEnums;
import icu.ydg.model.vo.homePageContentDisplay.HomePageContentDisplayVO;
import icu.ydg.model.vo.user.UserVO;
import icu.ydg.service.HomePageContentDisplayService;
import icu.ydg.service.UserService;
import icu.ydg.utils.RegexUtils;
import icu.ydg.utils.SqlUtils;
import icu.ydg.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 首页内容展示服务实现
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@Service
@Slf4j
public class HomePageContentDisplayServiceImpl extends ServiceImpl<HomePageContentDisplayMapper, HomePageContentDisplay> implements HomePageContentDisplayService {

    @Resource
    private UserService userService;
    @Resource
    private HomePageContentDisplayMapper homePageContentDisplayMapper;

    /**
     * 添加首页内容展示
     *
     * @param homePageContentDisplayAddRequest 发表评论添加请求
     * @param request                          请求
     * @return long
     */
    @Override
    public long addHomePageContentDisplay(HomePageContentDisplayAddRequest homePageContentDisplayAddRequest, HttpServletRequest request) {
        // todo 在此处将实体类和 DTO 进行转换
        HomePageContentDisplay homePageContentDisplay = new HomePageContentDisplay();
        BeanUtils.copyProperties(homePageContentDisplayAddRequest, homePageContentDisplay);
        // 数据校验
        validHomePageContentDisplay(homePageContentDisplay, true);
        // 写入数据库
        int insert = homePageContentDisplayMapper.insert(homePageContentDisplay);
        ThrowUtils.throwIf(insert <= 0, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newHomePageContentDisplayId = homePageContentDisplay.getId();
        // 返回新写入的数据 id
        return homePageContentDisplay.getId();
    }

    /**
     * 删除首页内容展示
     *
     * @param deleteRequest 删除请求
     * @param request       请求
     * @return long
     */
    @Override
    public long deleteHomePageContentDisplay(IdRequest deleteRequest, HttpServletRequest request) {
        long id = deleteRequest.getId();
        ThrowUtils.throwIf(homePageContentDisplayMapper.deleteById(id) <= 0, ErrorCode.OPERATION_ERROR);
        return id;
    }

    /**
     * 校验数据
     *
     * @param homePageContentDisplay
     * @param add                    对创建的数据进行校验
     */
    @Override
    public void validHomePageContentDisplay(HomePageContentDisplay homePageContentDisplay, boolean add) {
        ThrowUtils.throwIf(homePageContentDisplay == null, ErrorCode.PARAMS_ERROR);
        // 修改
        if (!add) {
            Long id = homePageContentDisplay.getId();
            ThrowUtils.throwIf(ObjectUtils.isEmpty(id) || id <= 0, ErrorCode.PARAMS_ERROR, "id非法");
        }
        String content = homePageContentDisplay.getContent();
        Integer type = homePageContentDisplay.getType();
        ThrowUtils.throwIf(HomePageContentStatusEnums.getEnumByValue(type) == null, "type非法");
        if (type == 0) {
            ThrowUtils.throwIf(StringUtils.isBlank(content) || content.length() > 255, "通知栏内容长度不符合要求");
        }
        if (type == 1) {
            // 校验链接正则表达式
            ThrowUtils.throwIf(!RegexUtils.matches(VerifyParamRegexConstant.WEBSITE_URL, content), "图片链接格式不正确");
        }
    }

    /**
     * 获取查询条件
     *
     * @param homePageContentDisplayQueryRequest
     * @return
     */
    @Override
    public LambdaQueryWrapper<HomePageContentDisplay> getQueryWrapper(HomePageContentDisplayQueryRequest homePageContentDisplayQueryRequest) {
        LambdaQueryWrapper<HomePageContentDisplay> queryWrapper = new LambdaQueryWrapper<>();
        if (homePageContentDisplayQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = homePageContentDisplayQueryRequest.getId();
        Long notId = homePageContentDisplayQueryRequest.getNotId();
        Integer type = homePageContentDisplayQueryRequest.getType();
        if (type != null) {
            HomePageContentStatusEnums enumByValue = HomePageContentStatusEnums.getEnumByValue(type);
            ThrowUtils.throwIf(enumByValue == null, ErrorCode.PARAMS_ERROR, "type非法");
        }
        String content = homePageContentDisplayQueryRequest.getContent();
        String sortField = homePageContentDisplayQueryRequest.getSortField();
        String sortOrder = homePageContentDisplayQueryRequest.getSortOrder();
        Long userId = homePageContentDisplayQueryRequest.getCreateBy();
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(content), HomePageContentDisplay::getContent, content);
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), HomePageContentDisplay::getId, notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(type), HomePageContentDisplay::getType, type);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), HomePageContentDisplay::getId, id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), HomePageContentDisplay::getCreateBy, userId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(PageConstant.SORT_ORDER_ASC), isSortField(sortField));
        return queryWrapper;
    }

    /**
     * 是否为排序字段
     *
     * @param sortField 排序字段
     * @return {@code SFunction<HomePageContentDisplay, ?>}
     */
    private SFunction<HomePageContentDisplay, ?> isSortField(String sortField) {
        if (Objects.equals(sortField, "")) {
            sortField = HomePageContentDisplaySortFieldEnums.UPDATE_TIME.name();
        }
        if (SqlUtils.validSortField(sortField)) {
            return HomePageContentDisplaySortFieldEnums.fromString(sortField).map(HomePageContentDisplaySortFieldEnums::getFieldGetter).orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "排序字段非法"));
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "排序字段无效，未知错误");
        }
    }

    /**
     * 获取首页内容展示封装
     *
     * @param idRequest
     * @param request
     * @return
     */
    @Override
    public HomePageContentDisplayVO getHomePageContentDisplayVO(IdRequest idRequest, HttpServletRequest request) {
        long id = idRequest.getId();
        // 查询数据库
        HomePageContentDisplay homePageContentDisplay = homePageContentDisplayMapper.selectById(id);
        ThrowUtils.throwIf(homePageContentDisplay == null, ErrorCode.NOT_FOUND_ERROR);
        // 对象转封装类
        HomePageContentDisplayVO homePageContentDisplayVO = HomePageContentDisplayVO.objToVo(homePageContentDisplay);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = homePageContentDisplay.getCreateBy();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        homePageContentDisplayVO.setUser(userVO);
        return homePageContentDisplayVO;
    }

    /**
     * “获取列表” 页
     *
     * @param homePageContentDisplayQueryRequest
     * @return {@link Page }<{@link HomePageContentDisplay }>
     */
    @Override
    public Page<HomePageContentDisplay> getListPage(HomePageContentDisplayQueryRequest homePageContentDisplayQueryRequest) {
        long current = homePageContentDisplayQueryRequest.getCurrent();
        long size = homePageContentDisplayQueryRequest.getPageSize();
        if (size == 0L) {
            size = PageConstant.PAGE_SIZE;
        }
        if (current == 0L) {
            current = PageConstant.CURRENT_PAGE;
        }
        // 查询数据库
        return this.page(new Page<>(current, size), this.getQueryWrapper(homePageContentDisplayQueryRequest));
    }

    /**
     * 分页获取首页内容展示封装
     *
     * @param homePageContentDisplayPage
     * @param request
     * @return
     */
    @Override
    public Page<HomePageContentDisplayVO> getHomePageContentDisplayVOPage(Page<HomePageContentDisplay> homePageContentDisplayPage, HttpServletRequest request) {
        List<HomePageContentDisplay> homePageContentDisplayList = homePageContentDisplayPage.getRecords();
        Page<HomePageContentDisplayVO> homePageContentDisplayVOPage = new Page<>(homePageContentDisplayPage.getCurrent(), homePageContentDisplayPage.getSize(), homePageContentDisplayPage.getTotal());
        if (CollUtil.isEmpty(homePageContentDisplayList)) {
            return homePageContentDisplayVOPage;
        }
        // 对象列表 => 封装对象列表
        List<HomePageContentDisplayVO> homePageContentDisplayVOList = homePageContentDisplayList.stream().map(homePageContentDisplay -> {
            return HomePageContentDisplayVO.objToVo(homePageContentDisplay);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = homePageContentDisplayList.stream().map(HomePageContentDisplay::getCreateBy).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        //Map<Long, Boolean> homePageContentDisplayIdHasPraiseMap = new HashMap<>();
        //Map<Long, Boolean> homePageContentDisplayIdHasCollectMap = new HashMap<>();
        //User loginUser = userService.getLoginUserPermitNull(request);
        //if (loginUser != null) {
        //    Set<Long> homePageContentDisplayIdSet = homePageContentDisplayList.stream().map(HomePageContentDisplay::getId).collect(Collectors.toSet());
        //    loginUser = userService.getLoginUser(request);
        //    // 获取点赞
        //    LambdaQueryWrapper<HomePageContentDisplayPraise> homePageContentDisplayPraiseQueryWrapper = new LambdaQueryWrapper<>();
        //    homePageContentDisplayPraiseQueryWrapper.in(HomePageContentDisplayPraise::getId, homePageContentDisplayIdSet);
        //    homePageContentDisplayPraiseQueryWrapper.eq(HomePageContentDisplayPraise::getCreateBy, loginUser.getId());
        //    List<HomePageContentDisplayPraise> homePageContentDisplayHomePageContentDisplayPraiseList = homePageContentDisplayThumbMapper.selectList(homePageContentDisplayThumbQueryWrapper);
        //    homePageContentDisplayHomePageContentDisplayThumbList.forEach(homePageContentDisplayHomePageContentDisplayPraise -> homePageContentDisplayIdHasPraiseMap.put(homePageContentDisplayHomePageContentDisplayPraise.getHomePageContentDisplayId(), true));
        //    // 获取收藏
        //    LambdaQueryWrapper<HomePageContentDisplayCollect> homePageContentDisplayCollectQueryWrapper = new LambdaQueryWrapper<>();
        //    homePageContentDisplayCollectQueryWrapper.in(HomePageContentDisplayCollect::getId, homePageContentDisplayIdSet);
        //    homePageContentDisplayCollectQueryWrapper.eq(HomePageContentDisplayCollect::getCreateBy, loginUser.getId());
        //    List<HomePageContentDisplayCollect> homePageContentDisplayCollectList = homePageContentDisplayCollectMapper.selectList(homePageContentDisplayCollectQueryWrapper);
        //    homePageContentDisplayCollectList.forEach(homePageContentDisplayCollect -> homePageContentDisplayIdHasCollectMap.put(homePageContentDisplayCollect.getHomePageContentDisplayId(), true));
        //}
        // 填充信息
        homePageContentDisplayVOList.forEach(homePageContentDisplayVO -> {
            Long userId = homePageContentDisplayVO.getCreateBy();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            homePageContentDisplayVO.setUser(userService.getUserVO(user));
        });
        // endregion

        homePageContentDisplayVOPage.setRecords(homePageContentDisplayVOList);
        return homePageContentDisplayVOPage;
    }

    /**
     * 更新首页内容展示
     *
     * @param homePageContentDisplayUpdateRequest 更新后请求
     * @param request                             请求
     * @return long
     */
    @Override
    public long updateHomePageContentDisplay(HomePageContentDisplayUpdateRequest homePageContentDisplayUpdateRequest, HttpServletRequest request) {
        long id = homePageContentDisplayUpdateRequest.getId();
        // 获取数据
        HomePageContentDisplay oldHomePageContentDisplay = homePageContentDisplayMapper.selectById(id);
        ThrowUtils.throwIf(oldHomePageContentDisplay == null, ErrorCode.NOT_FOUND_ERROR);
        validHomePageContentDisplay(oldHomePageContentDisplay, false);
        oldHomePageContentDisplay.setContent(homePageContentDisplayUpdateRequest.getContent());
        oldHomePageContentDisplay.setType(homePageContentDisplayUpdateRequest.getType());
        // 更新
        homePageContentDisplayMapper.updateById(oldHomePageContentDisplay);
        return id;
    }

    /**
     * 处理分页和验证
     *
     * @param homePageContentDisplayQueryRequest 首页内容展示查询请求
     * @param request                            请求
     * @return {@code Page<HomePageContentDisplayVO>}
     */
    @Override
    public Page<HomePageContentDisplayVO> handlePaginationAndValidation(HomePageContentDisplayQueryRequest homePageContentDisplayQueryRequest, HttpServletRequest request) {
        long current = homePageContentDisplayQueryRequest.getCurrent();
        long size = homePageContentDisplayQueryRequest.getPageSize();
        if (size == 0L) {
            size = PageConstant.PAGE_SIZE;
        }
        if (current == 0L) {
            current = PageConstant.CURRENT_PAGE;
        }
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<HomePageContentDisplay> homePageContentDisplayPage = this.page(new Page<>(current, size), this.getQueryWrapper(homePageContentDisplayQueryRequest));
        return this.getHomePageContentDisplayVOPage(homePageContentDisplayPage, request);
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
        HomePageContentDisplay oldHomePageContentDisplay = homePageContentDisplayMapper.selectById(id);
        ThrowUtils.throwIf(oldHomePageContentDisplay == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可操作（这里假设"编辑"和"删除"操作的权限是一样的）
        if (!oldHomePageContentDisplay.getCreateBy().equals(loginUser.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "你当前暂无该权限！");
        }
    }

    /**
     * 主页内容显示
     *
     * @return {@link Map }<{@link String }, {@link Object }>
     */
    @Override
    public Map<String, Object> homePageContentDisplay() {
        Map<String, Object> map = new HashMap<>();
        // 查询最近的一条公告，type=0,返回content
        HomePageContentDisplay notice = homePageContentDisplayMapper.selectOne(
                new LambdaQueryWrapper<HomePageContentDisplay>()
                        .orderByDesc(HomePageContentDisplay::getUpdateTime)
                        .eq(HomePageContentDisplay::getType, 0)
                        .select(HomePageContentDisplay::getContent)
                        .last("limit 1")
        );
        // 查询最近的5张轮播图,type=1,返回content
        List<HomePageContentDisplay> carousel = homePageContentDisplayMapper.selectList(
                new LambdaQueryWrapper<HomePageContentDisplay>()
                        .orderByDesc(HomePageContentDisplay::getUpdateTime)
                        .eq(HomePageContentDisplay::getType, 1)
                        .select(HomePageContentDisplay::getContent)
                        .last("limit 5")
        );
        List<String> carouselList = carousel.stream()
                .map(HomePageContentDisplay::getContent)
                .collect(Collectors.toList());
        // 添加
        map.put("notice", notice == null ? "暂无公告" : notice.getContent());
        map.put("carousel", carouselList);
        return map;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteHomePageContentDisplay(IdBatchRequest deleteRequest, HttpServletRequest request) {
        List<Long> ids = deleteRequest.getIds();
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("删除的ID列表不能为空");
        }

        // 执行批量删除
        this.removeByIds(ids); // MyBatis-Plus 提供的批量删除方法

        // 如果有其他相关的删除操作（比如关联表数据等），可以在这里进行处理
    }

}
