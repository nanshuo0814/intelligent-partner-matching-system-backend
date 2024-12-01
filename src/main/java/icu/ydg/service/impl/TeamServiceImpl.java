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
import icu.ydg.mapper.TeamMapper;
import icu.ydg.model.domain.Team;
import icu.ydg.model.domain.User;
import icu.ydg.model.domain.UserTeam;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.team.TeamAddRequest;
import icu.ydg.model.dto.team.TeamQueryRequest;
import icu.ydg.model.dto.team.TeamUpdateRequest;
import icu.ydg.model.enums.sort.TeamSortFieldEnums;
import icu.ydg.model.enums.team.TeamStatusEnums;
import icu.ydg.model.vo.team.TeamVO;
import icu.ydg.model.vo.user.UserVO;
import icu.ydg.service.TeamService;
import icu.ydg.service.UserService;
import icu.ydg.service.UserTeamService;
import icu.ydg.utils.SqlUtils;
import icu.ydg.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 队伍服务实现
 *
 * @author 袁德光
 * @Date 2024/11/03
 */
@Service
@Slf4j
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {

    @Resource
    private UserService userService;
    @Resource
    private TeamMapper teamMapper;
    @Resource
    @Lazy
    private UserTeamService userTeamService;
    @Resource
    @Lazy
    private TeamService teamService;

    // todo 如果后续需要点赞或收藏可自行添加

    /**
     * 添加队伍
     *
     * @param teamAddRequest 发表评论添加请求
     * @param request        请求
     * @return long
     */
    @Override
    public long addTeam(TeamAddRequest teamAddRequest, HttpServletRequest request) {
        // todo 在此处将实体类和 DTO 进行转换
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        String name = teamAddRequest.getName();
        ThrowUtils.throwIf(StringUtils.isBlank(name) || name.length() > 12, "队伍名称长度不合法");
        String description = teamAddRequest.getDescription();
        ThrowUtils.throwIf(StringUtils.isBlank(description) || description.length() > 512, "队伍描述长度不合法");
        String coverImage = teamAddRequest.getCoverImage();
        if (coverImage == null||coverImage.equals("")) {
            coverImage = "https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png";
        }
        team.setCoverImage(coverImage);
        Integer maxNum = teamAddRequest.getMaxNum();
        ThrowUtils.throwIf(maxNum == null || maxNum < 1 || maxNum > 100, "队伍人数不合法");
        Date expireTime = teamAddRequest.getExpireTime();
        // 不能为空且不能小于当前的时间
        ThrowUtils.throwIf(expireTime == null || expireTime.before(new Date()), "队伍过期时间不合法");
        Integer status = teamAddRequest.getStatus();
        // 通过枚举校验
        ThrowUtils.throwIf(TeamStatusEnums.getEnumByValue(status) == null, "队伍状态不合法");
        String password = teamAddRequest.getPassword();
        // 如果有密码，则密码长度不能超过 6 位
        ThrowUtils.throwIf(StringUtils.isNotBlank(password) && (password.length() > 6 || password.length() < 4), "队伍密码长度应为4-6位");
        // 写入数据库
        int insert = teamMapper.insert(team);
        ThrowUtils.throwIf(insert <= 0, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newTeamId = team.getId();
        // 写入UserTeam人数
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(newTeamId);
        userTeamService.save(userTeam);
        // 返回新写入的数据 id
        return team.getId();
    }

    /**
     * 删除队伍
     *
     * @param deleteRequest 删除请求
     * @param request       请求
     * @return long
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public long deleteTeam(IdRequest deleteRequest, HttpServletRequest request) {
        long id = deleteRequest.getId();
        onlyMeOrAdministratorCanDo(request, id);
        // 删除队伍，同时删除队伍成员
        LambdaQueryWrapper<UserTeam> qw = new LambdaQueryWrapper<>();
        qw.eq(UserTeam::getTeamId, id);
        userTeamService.remove(qw);
        ThrowUtils.throwIf(teamMapper.deleteById(id) <= 0, ErrorCode.OPERATION_ERROR);
        return id;
    }

    /**
     * 获取查询条件
     *
     * @param teamQueryRequest
     * @return
     */
    @Override
    public LambdaQueryWrapper<Team> getQueryWrapper(TeamQueryRequest teamQueryRequest) {
        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>();
        if (teamQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = teamQueryRequest.getId();
        Long notId = teamQueryRequest.getNotId();
        String title = teamQueryRequest.getName();
        String content = teamQueryRequest.getDescription();
        String searchText = teamQueryRequest.getSearchText();
        String sortField = teamQueryRequest.getSortField();
        String sortOrder = teamQueryRequest.getSortOrder();
        Long userId = teamQueryRequest.getCreateBy();
        Integer maxNum = teamQueryRequest.getMaxNum();
        Integer status = teamQueryRequest.getStatus();
        List<Long> teamIds = teamQueryRequest.getTeamIds();
        //List<String> tagList = teamQueryRequest.getTags();
        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // todo 需要拼接查询条件
            queryWrapper.and(qw -> qw.like(Team::getName, searchText).or().like(Team::getDescription, searchText));
        }
        // 模糊查询
        queryWrapper.in(null != teamIds && teamIds.size() > 0, Team::getId, teamIds);
        queryWrapper.like(StringUtils.isNotBlank(title), Team::getName, title);
        queryWrapper.like(StringUtils.isNotBlank(content), Team::getDescription, content);
        // JSON 数组查询
        //if (CollUtil.isNotEmpty(tagList)) {
        //    for (String tag : tagList) {
        //        queryWrapper.like(Team::getTags, "\"" + tag + "\"");
        //    }
        //}
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), Team::getId, notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), Team::getId, id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), Team::getCreateBy, userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(maxNum), Team::getMaxNum, maxNum);
        queryWrapper.eq(ObjectUtils.isNotEmpty(status), Team::getStatus, status);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(PageConstant.SORT_ORDER_ASC), isSortField(sortField));
        return queryWrapper;
    }

    /**
     * 是否为排序字段
     *
     * @param sortField 排序字段
     * @return {@code SFunction<Team, ?>}
     */
    private SFunction<Team, ?> isSortField(String sortField) {
        if (Objects.equals(sortField, "")) {
            sortField = TeamSortFieldEnums.UPDATE_TIME.name();
        }
        if (SqlUtils.validSortField(sortField)) {
            return TeamSortFieldEnums.fromString(sortField).map(TeamSortFieldEnums::getFieldGetter).orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "排序字段非法"));
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "排序字段无效，未知错误");
        }
    }

    /**
     * 获取队伍封装
     *
     * @param idRequest
     * @param request
     * @return
     */
    @Override
    public TeamVO getTeamVO(IdRequest idRequest, HttpServletRequest request) {
        long id = idRequest.getId();
        // 查询数据库
        Team team = teamMapper.selectById(id);
        ThrowUtils.throwIf(team == null, ErrorCode.NOT_FOUND_ERROR);
        // 对象转封装类
        TeamVO teamVO = TeamVO.objToVo(team);
        // region 可选
        // 1. 关联查询用户信息
        Long userId = team.getCreateBy();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        teamVO.setUser(userVO);
        // 2. 关联用户队伍信息
        LambdaQueryWrapper<UserTeam> userTeamQueryWrapper = new LambdaQueryWrapper<>();
        userTeamQueryWrapper.eq(UserTeam::getTeamId, id);
        userTeamQueryWrapper.select(UserTeam::getCreateBy);
        List<UserTeam> userTeams = userTeamService.list(userTeamQueryWrapper);
        // 3.从用户表里查询
        List<Long> userIds = userTeams.stream().map(UserTeam::getCreateBy).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(userIds)) {
            List<User> users = userService.listByIds(userIds);
            List<UserVO> userVOS = userService.getUserVOList(users);
            teamVO.setUserList(userVOS);
        } else {
            teamVO.setUserList(null);
        }
        return teamVO;
    }

    /**
     * “获取列表” 页
     *
     * @param teamQueryRequest
     * @return {@link Page }<{@link Team }>
     */
    @Override
    public Page<Team> getListPage(TeamQueryRequest teamQueryRequest) {
        long current = teamQueryRequest.getCurrent();
        long size = teamQueryRequest.getPageSize();
        if (size == 0L) {
            size = PageConstant.PAGE_SIZE;
        }
        if (current == 0L) {
            current = PageConstant.CURRENT_PAGE;
        }
        // 查询数据库
        return this.page(new Page<>(current, size), this.getQueryWrapper(teamQueryRequest));
    }

    /**
     * 分页获取队伍封装
     *
     * @param teamPage
     * @param request
     * @return
     */
    @Override
    public Page<TeamVO> getTeamVOPage(Page<Team> teamPage, HttpServletRequest request) {
        List<Team> teamList = teamPage.getRecords();
        Page<TeamVO> teamVOPage = new Page<>(teamPage.getCurrent(), teamPage.getSize(), teamPage.getTotal());
        if (CollUtil.isEmpty(teamList)) {
            return teamVOPage;
        }
        // 对象列表 => 封装对象列表
        List<TeamVO> teamVOList = teamList.stream().map(team -> {
            return TeamVO.objToVo(team);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = teamList.stream().map(Team::getCreateBy).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        //Map<Long, Boolean> teamIdHasPraiseMap = new HashMap<>();
        //Map<Long, Boolean> teamIdHasCollectMap = new HashMap<>();
        //User loginUser = userService.getLoginUserPermitNull(request);
        //if (loginUser != null) {
        //    Set<Long> teamIdSet = teamList.stream().map(Team::getId).collect(Collectors.toSet());
        //    loginUser = userService.getLoginUser(request);
        //    // 获取点赞
        //    LambdaQueryWrapper<TeamPraise> teamPraiseQueryWrapper = new LambdaQueryWrapper<>();
        //    teamPraiseQueryWrapper.in(TeamPraise::getId, teamIdSet);
        //    teamPraiseQueryWrapper.eq(TeamPraise::getCreateBy, loginUser.getId());
        //    List<TeamPraise> teamTeamPraiseList = teamThumbMapper.selectList(teamThumbQueryWrapper);
        //    teamTeamThumbList.forEach(teamTeamPraise -> teamIdHasPraiseMap.put(teamTeamPraise.getTeamId(), true));
        //    // 获取收藏
        //    LambdaQueryWrapper<TeamCollect> teamCollectQueryWrapper = new LambdaQueryWrapper<>();
        //    teamCollectQueryWrapper.in(TeamCollect::getId, teamIdSet);
        //    teamCollectQueryWrapper.eq(TeamCollect::getCreateBy, loginUser.getId());
        //    List<TeamCollect> teamCollectList = teamCollectMapper.selectList(teamCollectQueryWrapper);
        //    teamCollectList.forEach(teamCollect -> teamIdHasCollectMap.put(teamCollect.getTeamId(), true));
        //}
        // 填充信息
        teamVOList.forEach(teamVO -> {
            Long userId = teamVO.getCreateBy();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            teamVO.setUser(userService.getUserVO(user));
        });
        // endregion

        teamVOPage.setRecords(teamVOList);
        return teamVOPage;
    }

    /**
     * 更新队伍
     *
     * @param teamUpdateRequest 更新后请求
     * @param request           请求
     * @return long
     */
    @Override
    public long updateTeam(TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        // 本人和管理员可修改
        User user = userService.getLoginUser(request);
        if (!UserConstant.ADMIN_ROLE.equals(user.getUserRole()) && !Objects.equals(user.getId(), teamUpdateRequest.getCreateBy())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        long id = teamUpdateRequest.getId();
        // 获取数据
        Team oldTeam = teamMapper.selectById(id);
        ThrowUtils.throwIf(oldTeam == null, ErrorCode.NOT_FOUND_ERROR);
        // todo 设置值
        String name = teamUpdateRequest.getName();
        ThrowUtils.throwIf(StringUtils.isBlank(name) || name.length() > 12, "队伍名称长度不合法");
        String description = teamUpdateRequest.getDescription();
        ThrowUtils.throwIf(StringUtils.isBlank(description) || description.length() > 512, "队伍描述长度不合法");
        String coverImage = teamUpdateRequest.getCoverImage();
        Integer maxNum = teamUpdateRequest.getMaxNum();
        ThrowUtils.throwIf(maxNum == null || maxNum < 1 || maxNum > 100 || maxNum < oldTeam.getCurrentNum(), "队伍人数不合法");
        Date expireTime = teamUpdateRequest.getExpireTime();
        // 不能为空且不能小于当前的时间
        if(expireTime != null) {
            ThrowUtils.throwIf(expireTime.before(new Date()), "队伍过期时间不合法"); 
        } else {
            teamUpdateRequest.setExpireTime(oldTeam.getExpireTime());
        }
        Integer status = teamUpdateRequest.getStatus();
        ThrowUtils.throwIf(TeamStatusEnums.getEnumByValue(status) == null, "队伍状态不合法");
        String password = teamUpdateRequest.getPassword();
        // 如果有密码，则密码长度不能超过 6 位
        ThrowUtils.throwIf(StringUtils.isNotBlank(password) && (password.length() > 6 || password.length() < 3), "队伍密码长度应为4-6位");
        // 更新
        BeanUtils.copyProperties(teamUpdateRequest, oldTeam);
        teamMapper.updateById(oldTeam);
        return id;
    }

    /**
     * 处理分页和验证
     *
     * @param teamQueryRequest 队伍查询请求
     * @param request          请求
     * @return {@code Page<TeamVO>}
     */
    @Override
    public Page<TeamVO> handlePaginationAndValidation(TeamQueryRequest teamQueryRequest, HttpServletRequest request) {
        long current = teamQueryRequest.getCurrent();
        long size = teamQueryRequest.getPageSize();
        if (size == 0L) {
            size = PageConstant.PAGE_SIZE;
        }
        if (current == 0L) {
            current = PageConstant.CURRENT_PAGE;
        }
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Team> teamPage = this.page(new Page<>(current, size), this.getQueryWrapper(teamQueryRequest));
        return this.getTeamVOPage(teamPage, request);
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
        Team oldTeam = teamMapper.selectById(id);
        ThrowUtils.throwIf(oldTeam == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可操作（这里假设"编辑"和"删除"操作的权限是一样的）
        if (!oldTeam.getCreateBy().equals(loginUser.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "你当前暂无该权限！");
        }
    }

    /**
     * 团队num
     *
     * @param userId 用户id
     * @return long
     */
    @Override
    public long getUnReadTeamNum(Long userId) {
        //LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        //queryWrapper.eq(UserTeam::getUserId, userId)
        //            .isNull(UserTeam::getReadTime);
        //return userTeamService.count(queryWrapper);
        return 1;
    }

    /**
     * 分页查询用户加入的队伍信息
     *
     * @param userId           当前用户 ID
     * @param teamQueryRequest 分页查询参数
     * @return 分页结果
     */
    // public Page<TeamVO> getTeamVOByIdsWithPagination(TeamQueryRequest teamQueryRequest) {

    //     // 构建分页对象
    //     Page<TeamVO> page = new Page<>(teamQueryRequest.getCurrent(), teamQueryRequest.getPageSize());

    //     // 查询队伍信息并分页
    //     LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>();
    //     queryWrapper.in(Team::getId, teamQueryRequest.getTeamIds())
    //                 .like(StringUtils.isNotBlank(teamQueryRequest.getSearchText()), Team::getName, teamQueryRequest.getSearchText())
    //             .orderByDesc(Team::getUpdateBy);
    // return getQueryWrapper(page,queryWrapper);
    // }
}
