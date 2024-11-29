package icu.ydg.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.ydg.common.ErrorCode;
import icu.ydg.constant.PageConstant;
import icu.ydg.constant.UserConstant;
import icu.ydg.exception.BusinessException;
import icu.ydg.mapper.TeamMapper;
import icu.ydg.mapper.UserTeamMapper;
import icu.ydg.model.domain.Team;
import icu.ydg.model.domain.User;
import icu.ydg.model.domain.UserTeam;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.team.TeamQueryRequest;
import icu.ydg.model.dto.userTeam.UserTeamAddRequest;
import icu.ydg.model.dto.userTeam.UserTeamQueryRequest;
import icu.ydg.model.dto.userTeam.UserTeamUpdateRequest;
import icu.ydg.model.enums.sort.UserTeamSortFieldEnums;
import icu.ydg.model.vo.team.TeamVO;
import icu.ydg.model.vo.user.UserVO;
import icu.ydg.model.vo.userTeam.UserTeamVO;
import icu.ydg.service.TeamService;
import icu.ydg.service.UserService;
import icu.ydg.service.UserTeamService;
import icu.ydg.utils.SqlUtils;
import icu.ydg.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户队伍服务实现
 *
 * @author 袁德光
 * @Date 2024/11/03
 */
@Service
@Slf4j
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam> implements UserTeamService {

    @Resource
    private UserService userService;
    @Resource
    private UserTeamMapper userTeamMapper;
    @Resource
    private TeamService teamService;

    /**
     * 根据用户 ID 获取其加入的队伍 ID 列表
     *
     * @param userId 用户 ID
     * @return 队伍 ID 列表
     */
    public List<Long> getJoinedTeamIdsByUserId(Long userId) {
        LambdaQueryWrapper<UserTeam> qw = new LambdaQueryWrapper<>();
        qw.eq(UserTeam::getCreateBy, userId);
        qw.select(UserTeam::getTeamId);
        List<UserTeam> selectList = userTeamMapper.selectList(qw);
        // 转为队伍 ID 列表
        return selectList.stream().map(UserTeam::getTeamId).collect(Collectors.toList());
    }
    /**
     * 添加用户队伍
     *
     * @param userTeamAddRequest 发表评论添加请求
     * @param request            请求
     * @return long
     */
    @Override
    public long addUserTeam(UserTeamAddRequest userTeamAddRequest, HttpServletRequest request) {
        Long teamId = userTeamAddRequest.getTeamId();
        // 校验队伍是否存在
        Team team = teamService.getById(teamId);
        ThrowUtils.throwIf(team == null, "队伍不存在");
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        // 校验自己是否已在队伍中
        long count = userTeamMapper.selectCount(new LambdaQueryWrapper<UserTeam>()
                .eq(UserTeam::getTeamId, teamId)
                .eq(UserTeam::getCreateBy, loginUser.getId())
        );
        ThrowUtils.throwIf(count > 0, "已在队伍中");
        // 校验队伍是否满员
        Integer maxNum = team.getMaxNum();
        Integer currentNum = team.getCurrentNum();
        ThrowUtils.throwIf(currentNum >= maxNum, "队伍已满员");
        Integer status = team.getStatus();
        if (status == 1) {
            ThrowUtils.throwIfNull(true, "队伍私有");
        }
        if (status == 2) {
            String teamPwd = team.getPassword();
            String password = userTeamAddRequest.getPassword();
            ThrowUtils.throwIf(password == null || !teamPwd.equals(password.toString()), "队伍密码错误");
        }
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        // 数据校验
        validUserTeam(userTeam, true);
        // 写入数据库
        int insert = userTeamMapper.insert(userTeam);
        ThrowUtils.throwIf(insert <= 0, ErrorCode.OPERATION_ERROR);
        // 执行队伍表人数加1
        team.setCurrentNum(team.getCurrentNum() + 1);
        ThrowUtils.throwIf(!teamService.updateById(team), ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newUserTeamId = userTeam.getId();
        // 返回新写入的数据 id
        return userTeam.getId();
    }

    /**
     * 删除用户队伍
     *
     * @param deleteRequest 删除请求
     * @param request       请求
     * @return long
     */
    @Override
    public long deleteUserTeam(IdRequest deleteRequest, HttpServletRequest request) {
        long id = deleteRequest.getId();
        onlyMeOrAdministratorCanDo(request, id);
        // 执行队伍表人数减-1
        UserTeam userTeam = userTeamMapper.selectById(id);
        // 判断是否在队伍中
        Long userId = userTeam.getCreateBy();
        Long teamId = userTeam.getTeamId();
        Long count = userTeamMapper.selectCount(new LambdaQueryWrapper<UserTeam>()
                .eq(UserTeam::getTeamId, teamId)
                .eq(UserTeam::getCreateBy, userId)
        );
        ThrowUtils.throwIf(count <= 0, "你不在队伍中");
        Team byId = teamService.getById(teamId);
        byId.setCurrentNum(byId.getCurrentNum() - 1);
        ThrowUtils.throwIf(!teamService.updateById(byId), ErrorCode.OPERATION_ERROR);
        ThrowUtils.throwIf(userTeamMapper.deleteById(id) <= 0, ErrorCode.OPERATION_ERROR);
        return id;
    }

    /**
     * 校验数据
     *
     * @param userTeam
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validUserTeam(UserTeam userTeam, boolean add) {
        ThrowUtils.throwIf(userTeam == null, ErrorCode.PARAMS_ERROR);
        // 修改数据时的校验规则
        if (!add) {
            Long id = userTeam.getId();
            ThrowUtils.throwIf(id == null || id <= 0L, ErrorCode.PARAMS_ERROR, "队伍id非法");
            Long createBy = userTeam.getCreateBy();
            ThrowUtils.throwIf(createBy == null || createBy <= 0L, ErrorCode.PARAMS_ERROR, "创建人id非法");
        }
        // 修改和添加的公共规则
    }

    /**
     * 获取查询条件
     *
     * @param userTeamQueryRequest
     * @return
     */
    @Override
    public LambdaQueryWrapper<UserTeam> getQueryWrapper(UserTeamQueryRequest userTeamQueryRequest) {
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        if (userTeamQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = userTeamQueryRequest.getId();
        Long notId = userTeamQueryRequest.getNotId();
        String title = userTeamQueryRequest.getTitle();
        String content = userTeamQueryRequest.getContent();
        String searchText = userTeamQueryRequest.getSearchText();
        String sortField = userTeamQueryRequest.getSortField();
        String sortOrder = userTeamQueryRequest.getSortOrder();
        Long userId = userTeamQueryRequest.getCreateBy();
        //List<String> tagList = userTeamQueryRequest.getTags();
        // todo 补充需要的查询条件
        // 从多字段中搜索
        //if (StringUtils.isNotBlank(searchText)) {
        //    // todo 需要拼接查询条件
        //    queryWrapper.and(qw -> qw.like(UserTeam::getTitle, searchText).or().like(UserTeam::getContent, searchText));
        //}
        // 模糊查询
        //queryWrapper.like(StringUtils.isNotBlank(title), UserTeam::getTitle, title);
        //queryWrapper.like(StringUtils.isNotBlank(content), UserTeam::getContent, content);
        // JSON 数组查询
        //if (CollUtil.isNotEmpty(tagList)) {
        //    for (String tag : tagList) {
        //        queryWrapper.like(UserTeam::getTags, "\"" + tag + "\"");
        //    }
        //}
        // 精确查询
        //queryWrapper.ne(ObjectUtils.isNotEmpty(notId), UserTeam::getId, notId);
        //queryWrapper.eq(ObjectUtils.isNotEmpty(id), UserTeam::getId, id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), UserTeam::getCreateBy, userId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(PageConstant.SORT_ORDER_ASC), isSortField(sortField));
        return queryWrapper;
    }

    /**
     * 是否为排序字段
     *
     * @param sortField 排序字段
     * @return {@code SFunction<UserTeam, ?>}
     */
    private SFunction<UserTeam, ?> isSortField(String sortField) {
        if (Objects.equals(sortField, "")) {
            sortField = UserTeamSortFieldEnums.UPDATE_TIME.name();
        }
        if (SqlUtils.validSortField(sortField)) {
            return UserTeamSortFieldEnums.fromString(sortField).map(UserTeamSortFieldEnums::getFieldGetter).orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "排序字段非法"));
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "排序字段无效，未知错误");
        }
    }

    /**
     * 获取用户队伍封装
     *
     * @param idRequest
     * @param request
     * @return
     */
    @Override
    public UserTeamVO getUserTeamVO(IdRequest idRequest, HttpServletRequest request) {
        long id = idRequest.getId();
        // 查询数据库
        UserTeam userTeam = userTeamMapper.selectById(id);
        ThrowUtils.throwIf(userTeam == null, ErrorCode.NOT_FOUND_ERROR);
        // 对象转封装类
        UserTeamVO userTeamVO = UserTeamVO.objToVo(userTeam);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = userTeam.getCreateBy();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        userTeamVO.setUser(userVO);
        return userTeamVO;
    }

    /**
     * “获取列表” 页
     *
     * @param userTeamQueryRequest
     * @return {@link Page }<{@link UserTeam }>
     */
    @Override
    public Page<UserTeam> getListPage(UserTeamQueryRequest userTeamQueryRequest) {
        long current = userTeamQueryRequest.getCurrent();
        long size = userTeamQueryRequest.getPageSize();
        if (size == 0L) {
            size = PageConstant.PAGE_SIZE;
        }
        if (current == 0L) {
            current = PageConstant.CURRENT_PAGE;
        }
        // 查询数据库
        return this.page(new Page<>(current, size), this.getQueryWrapper(userTeamQueryRequest));
    }

    /**
     * 分页获取用户队伍封装
     *
     * @param userTeamPage
     * @param request
     * @return
     */
    @Override
    public Page<UserTeamVO> getUserTeamVOPage(Page<UserTeam> userTeamPage, HttpServletRequest request) {
        List<UserTeam> userTeamList = userTeamPage.getRecords();
        Page<UserTeamVO> userTeamVOPage = new Page<>(userTeamPage.getCurrent(), userTeamPage.getSize(), userTeamPage.getTotal());
        if (CollUtil.isEmpty(userTeamList)) {
            return userTeamVOPage;
        }
        // 对象列表 => 封装对象列表
        List<UserTeamVO> userTeamVOList = userTeamList.stream().map(userTeam -> {
            return UserTeamVO.objToVo(userTeam);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = userTeamList.stream().map(UserTeam::getCreateBy).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        //Map<Long, Boolean> userTeamIdHasPraiseMap = new HashMap<>();
        //Map<Long, Boolean> userTeamIdHasCollectMap = new HashMap<>();
        //User loginUser = userService.getLoginUserPermitNull(request);
        //if (loginUser != null) {
        //    Set<Long> userTeamIdSet = userTeamList.stream().map(UserTeam::getId).collect(Collectors.toSet());
        //    loginUser = userService.getLoginUser(request);
        //    // 获取点赞
        //    LambdaQueryWrapper<UserTeamPraise> userTeamPraiseQueryWrapper = new LambdaQueryWrapper<>();
        //    userTeamPraiseQueryWrapper.in(UserTeamPraise::getId, userTeamIdSet);
        //    userTeamPraiseQueryWrapper.eq(UserTeamPraise::getCreateBy, loginUser.getId());
        //    List<UserTeamPraise> userTeamUserTeamPraiseList = userTeamThumbMapper.selectList(userTeamThumbQueryWrapper);
        //    userTeamUserTeamThumbList.forEach(userTeamUserTeamPraise -> userTeamIdHasPraiseMap.put(userTeamUserTeamPraise.getUserTeamId(), true));
        //    // 获取收藏
        //    LambdaQueryWrapper<UserTeamCollect> userTeamCollectQueryWrapper = new LambdaQueryWrapper<>();
        //    userTeamCollectQueryWrapper.in(UserTeamCollect::getId, userTeamIdSet);
        //    userTeamCollectQueryWrapper.eq(UserTeamCollect::getCreateBy, loginUser.getId());
        //    List<UserTeamCollect> userTeamCollectList = userTeamCollectMapper.selectList(userTeamCollectQueryWrapper);
        //    userTeamCollectList.forEach(userTeamCollect -> userTeamIdHasCollectMap.put(userTeamCollect.getUserTeamId(), true));
        //}
        // 填充信息
        userTeamVOList.forEach(userTeamVO -> {
            Long userId = userTeamVO.getCreateBy();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            userTeamVO.setUser(userService.getUserVO(user));
        });
        // endregion

        userTeamVOPage.setRecords(userTeamVOList);
        return userTeamVOPage;
    }

    /**
     * 更新用户队伍
     *
     * @param userTeamUpdateRequest 更新后请求
     * @param request               请求
     * @return long
     */
    @Override
    public long updateUserTeam(UserTeamUpdateRequest userTeamUpdateRequest, HttpServletRequest request) {
        // 本人和管理员可修改
        User user = userService.getLoginUser(request);
        if (!UserConstant.ADMIN_ROLE.equals(user.getUserRole()) && !Objects.equals(user.getId(), userTeamUpdateRequest.getCreateBy())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        long id = userTeamUpdateRequest.getId();
        // 获取数据
        UserTeam oldUserTeam = userTeamMapper.selectById(id);
        ThrowUtils.throwIf(oldUserTeam == null, ErrorCode.NOT_FOUND_ERROR);
        // todo 设置值
        //oldUserTeam.setTitle(userTeamUpdateRequest.getTitle());
        //oldUserTeam.setContent(userTeamUpdateRequest.getContent());
        // 参数校验
        validUserTeam(oldUserTeam, false);
        // 更新
        userTeamMapper.updateById(oldUserTeam);
        return id;
    }

    /**
     * 处理分页和验证
     *
     * @param userTeamQueryRequest 用户队伍查询请求
     * @param request              请求
     * @return {@code Page<UserTeamVO>}
     */
    @Override
    public Page<UserTeamVO> handlePaginationAndValidation(UserTeamQueryRequest userTeamQueryRequest, HttpServletRequest request) {
        long current = userTeamQueryRequest.getCurrent();
        long size = userTeamQueryRequest.getPageSize();
        if (size == 0L) {
            size = PageConstant.PAGE_SIZE;
        }
        if (current == 0L) {
            current = PageConstant.CURRENT_PAGE;
        }
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<UserTeam> userTeamPage = this.page(new Page<>(current, size), this.getQueryWrapper(userTeamQueryRequest));
        return this.getUserTeamVOPage(userTeamPage, request);
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
        UserTeam oldUserTeam = userTeamMapper.selectById(id);
        ThrowUtils.throwIf(oldUserTeam == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可操作（这里假设"编辑"和"删除"操作的权限是一样的）
        if (!oldUserTeam.getCreateBy().equals(loginUser.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "你当前暂无该权限！");
        }
    }


}
