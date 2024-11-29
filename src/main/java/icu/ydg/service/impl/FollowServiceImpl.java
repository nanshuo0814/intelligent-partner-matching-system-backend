package icu.ydg.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.ydg.common.ErrorCode;
import icu.ydg.constant.PageConstant;
import icu.ydg.constant.RedisKeyConstant;
import icu.ydg.constant.UserConstant;
import icu.ydg.exception.BusinessException;
import icu.ydg.mapper.FollowMapper;
import icu.ydg.model.domain.Follow;
import icu.ydg.model.domain.User;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.follow.FollowAddRequest;
import icu.ydg.model.dto.follow.FollowQueryRequest;
import icu.ydg.model.dto.follow.FollowUpdateRequest;
import icu.ydg.model.enums.sort.FollowSortFieldEnums;
import icu.ydg.model.vo.follow.FollowVO;
import icu.ydg.model.vo.user.UserVO;
import icu.ydg.service.FollowService;
import icu.ydg.service.MessageService;
import icu.ydg.service.UserService;
import icu.ydg.utils.SqlUtils;
import icu.ydg.utils.ThrowUtils;
import icu.ydg.utils.redis.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 关注服务实现
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@Service
@Slf4j
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    @Resource
    private UserService userService;
    @Resource
    private FollowMapper followMapper;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private MessageService messageService;

    /**
     * 添加关注
     *
     * @param followAddRequest 发表评论添加请求
     * @param request          请求
     * @return long
     */
    @Override
    public long addFollow(FollowAddRequest followAddRequest, HttpServletRequest request) {
        Follow follow = new Follow();
        BeanUtils.copyProperties(followAddRequest, follow);
        // 数据校验
        validFollow(follow, true);
        Long followUserId = followAddRequest.getFollowUserId();
        User byId = userService.getById(followUserId);
        ThrowUtils.throwIf(byId == null, ErrorCode.PARAMS_ERROR, "关注用户不存在");
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser.getId().equals(followUserId), ErrorCode.PARAMS_ERROR, "不能关注自己");
        // 不能重复关注他人
        LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Follow::getFollowUserId, followUserId);
        queryWrapper.eq(Follow::getCreateBy, loginUser.getId());
        Follow one = followMapper.selectOne(queryWrapper);
        ThrowUtils.throwIf(one != null, ErrorCode.PARAMS_ERROR, "不能重复关注他人");
        // 写入数据库
        int insert = followMapper.insert(follow);
        ThrowUtils.throwIf(insert <= 0, ErrorCode.OPERATION_ERROR);
        // 关注数+1（粉丝）
        redisUtils.incr(RedisKeyConstant.MESSAGE_FANS_NUM_KEY + followUserId, 1);
        // 储存消息
        //Message message = new Message();
        //message.setType(MessageTypeEnums.FANS.getValue());
        //message.setCreateBy(loginUser.getId());
        //message.setToId(followUserId);
        //message.setContent(String.valueOf(follow.getId()));
        //messageService.save(message);
        // 返回新写入的数据 id
        long newFollowId = follow.getId();
        // 返回新写入的数据 id
        return follow.getId();
    }

    /**
     * 删除关注
     *
     * @param deleteRequest 删除请求
     * @param request       请求
     * @return long
     */
    @Override
    public long deleteFollow(IdRequest deleteRequest, HttpServletRequest request) {
        long id = deleteRequest.getId();
        onlyMeOrAdministratorCanDo(request, id);
        ThrowUtils.throwIf(followMapper.deleteById(id) <= 0, ErrorCode.OPERATION_ERROR);
        return id;
    }

    /**
     * 校验数据
     *
     * @param follow
     * @param add    对创建的数据进行校验
     */
    @Override
    public void validFollow(Follow follow, boolean add) {
        ThrowUtils.throwIf(follow == null, ErrorCode.PARAMS_ERROR);
        //     这里就实现了添加好友关注，修改好友关注并不需要校验，这里以后可以扩展，添加多一个数据库表字段拉黑属性字段
        Long followUserId = follow.getFollowUserId();
        ThrowUtils.throwIf(add && ObjectUtils.isEmpty(followUserId) || followUserId <= 0, ErrorCode.PARAMS_ERROR, "关注用户不能为空");
    }

    /**
     * 获取查询条件
     *
     * @param followQueryRequest
     * @return
     */
    @Override
    public LambdaQueryWrapper<Follow> getQueryWrapper(FollowQueryRequest followQueryRequest) {
        LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
        if (followQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = followQueryRequest.getId();
        Long notId = followQueryRequest.getNotId();
        String title = followQueryRequest.getTitle();
        String content = followQueryRequest.getContent();
        String searchText = followQueryRequest.getSearchText();
        String sortField = followQueryRequest.getSortField();
        String sortOrder = followQueryRequest.getSortOrder();
        Long userId = followQueryRequest.getCreateBy();
        Long followUserId = followQueryRequest.getFollowUserId();
        //List<String> tagList = followQueryRequest.getTags();
        // todo 补充需要的查询条件
        // 从多字段中搜索
        //if (StringUtils.isNotBlank(searchText)) {
        //    // todo 需要拼接查询条件
        //    queryWrapper.and(qw -> qw.like(Follow::getTitle, searchText).or().like(Follow::getContent, searchText));
        //}
        // 模糊查询
        //queryWrapper.like(StringUtils.isNotBlank(title), Follow::getTitle, title);
        //queryWrapper.like(StringUtils.isNotBlank(content), Follow::getContent, content);
        // JSON 数组查询
        //if (CollUtil.isNotEmpty(tagList)) {
        //    for (String tag : tagList) {
        //        queryWrapper.like(Follow::getTags, "\"" + tag + "\"");
        //    }
        //}
        // 精确查询
        //queryWrapper.ne(ObjectUtils.isNotEmpty(notId), Follow::getId, notId);
        //queryWrapper.eq(ObjectUtils.isNotEmpty(id), Follow::getId, id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), Follow::getCreateBy, userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(followUserId), Follow::getFollowUserId, followUserId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(PageConstant.SORT_ORDER_ASC), isSortField(sortField));
        return queryWrapper;
    }

    /**
     * 是否为排序字段
     *
     * @param sortField 排序字段
     * @return {@code SFunction<Follow, ?>}
     */
    private SFunction<Follow, ?> isSortField(String sortField) {
        if (Objects.equals(sortField, "")) {
            sortField = FollowSortFieldEnums.UPDATE_TIME.name();
        }
        if (SqlUtils.validSortField(sortField)) {
            return FollowSortFieldEnums.fromString(sortField).map(FollowSortFieldEnums::getFieldGetter).orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "排序字段非法"));
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "排序字段无效，未知错误");
        }
    }

    /**
     * 获取关注封装
     *
     * @param idRequest
     * @param request
     * @return
     */
    @Override
    public FollowVO getFollowVO(IdRequest idRequest, HttpServletRequest request) {
        long id = idRequest.getId();
        // 查询数据库
        Follow follow = followMapper.selectById(id);
        ThrowUtils.throwIf(follow == null, ErrorCode.NOT_FOUND_ERROR);
        // 对象转封装类
        FollowVO followVO = FollowVO.objToVo(follow);
        // region 可选
        // 1. 关联查询用户信息
        Long userId = follow.getFollowUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        followVO.setUser(userVO);
        return followVO;
    }

    /**
     * “获取列表” 页
     *
     * @param followQueryRequest
     * @return {@link Page }<{@link Follow }>
     */
    @Override
    public Page<Follow> getListPage(FollowQueryRequest followQueryRequest) {
        long current = followQueryRequest.getCurrent();
        long size = followQueryRequest.getPageSize();
        if (size == 0L) {
            size = PageConstant.PAGE_SIZE;
        }
        if (current == 0L) {
            current = PageConstant.CURRENT_PAGE;
        }
        // 查询数据库
        return this.page(new Page<>(current, size), this.getQueryWrapper(followQueryRequest));
    }

    /**
     * 分页获取关注封装
     *
     * @param followPage
     * @param request
     * @return
     */
    @Override
    public Page<FollowVO> getFollowVOPage(Page<Follow> followPage, HttpServletRequest request) {
        List<Follow> followList = followPage.getRecords();
        Page<FollowVO> followVOPage = new Page<>(followPage.getCurrent(), followPage.getSize(), followPage.getTotal());
        if (CollUtil.isEmpty(followList)) {
            return followVOPage;
        }
        // 对象列表 => 封装对象列表
        List<FollowVO> followVOList = followList.stream().map(FollowVO::objToVo).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = followList.stream().map(Follow::getCreateBy).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        //Map<Long, Boolean> followIdHasPraiseMap = new HashMap<>();
        //Map<Long, Boolean> followIdHasCollectMap = new HashMap<>();
        //User loginUser = userService.getLoginUserPermitNull(request);
        //if (loginUser != null) {
        //    Set<Long> followIdSet = followList.stream().map(Follow::getId).collect(Collectors.toSet());
        //    loginUser = userService.getLoginUser(request);
        //    // 获取点赞
        //    LambdaQueryWrapper<FollowPraise> followPraiseQueryWrapper = new LambdaQueryWrapper<>();
        //    followPraiseQueryWrapper.in(FollowPraise::getId, followIdSet);
        //    followPraiseQueryWrapper.eq(FollowPraise::getCreateBy, loginUser.getId());
        //    List<FollowPraise> followFollowPraiseList = followThumbMapper.selectList(followThumbQueryWrapper);
        //    followFollowThumbList.forEach(followFollowPraise -> followIdHasPraiseMap.put(followFollowPraise.getFollowId(), true));
        //    // 获取收藏
        //    LambdaQueryWrapper<FollowCollect> followCollectQueryWrapper = new LambdaQueryWrapper<>();
        //    followCollectQueryWrapper.in(FollowCollect::getId, followIdSet);
        //    followCollectQueryWrapper.eq(FollowCollect::getCreateBy, loginUser.getId());
        //    List<FollowCollect> followCollectList = followCollectMapper.selectList(followCollectQueryWrapper);
        //    followCollectList.forEach(followCollect -> followIdHasCollectMap.put(followCollect.getFollowId(), true));
        //}
        // 填充信息
        Set<Long> followIdSet = followList.stream().map(Follow::getFollowUserId).collect(Collectors.toSet());
        Map<Long, List<User>> followIdUserListMap = userService.listByIds(followIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        followVOList.forEach(followVO -> {
            Long userId = followVO.getCreateBy();
            Long followUserId = followVO.getFollowUserId();
            User user = null;
            User followUser = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            if (followIdUserListMap.containsKey(followUserId)) {
                followUser = followIdUserListMap.get(followUserId).get(0);
            }
            followVO.setUser(userService.getUserVO(user));
            followVO.setFollowUser(userService.getUserVO(followUser));
        });

        // endregion

        followVOPage.setRecords(followVOList);
        return followVOPage;
    }

    /**
     * 更新关注
     *
     * @param followUpdateRequest 更新后请求
     * @param request             请求
     * @return long
     */
    @Override
    public long updateFollow(FollowUpdateRequest followUpdateRequest, HttpServletRequest request) {
        // 本人和管理员可修改
        User user = userService.getLoginUser(request);
        if (!UserConstant.ADMIN_ROLE.equals(user.getUserRole()) && !Objects.equals(user.getId(), followUpdateRequest.getCreateBy())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        long id = followUpdateRequest.getId();
        // 获取数据
        Follow oldFollow = followMapper.selectById(id);
        ThrowUtils.throwIf(oldFollow == null, ErrorCode.NOT_FOUND_ERROR);
        // todo 设置值
        //oldFollow.setTitle(followUpdateRequest.getTitle());
        //oldFollow.setContent(followUpdateRequest.getContent());
        // 参数校验
        validFollow(oldFollow, false);
        // 更新
        followMapper.updateById(oldFollow);
        return id;
    }

    /**
     * 处理分页和验证
     *
     * @param followQueryRequest 关注查询请求
     * @param request            请求
     * @return {@code Page<FollowVO>}
     */
    @Override
    public Page<FollowVO> handlePaginationAndValidation(FollowQueryRequest followQueryRequest, HttpServletRequest request) {
        long current = followQueryRequest.getCurrent();
        long size = followQueryRequest.getPageSize();
        if (size == 0L) {
            size = PageConstant.PAGE_SIZE;
        }
        if (current == 0L) {
            current = PageConstant.CURRENT_PAGE;
        }
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Follow> followPage = this.page(new Page<>(current, size), this.getQueryWrapper(followQueryRequest));
        return this.getFollowVOPage(followPage, request);
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
        Follow oldFollow = followMapper.selectById(id);
        ThrowUtils.throwIf(oldFollow == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可操作（这里假设"编辑"和"删除"操作的权限是一样的）
        if (!oldFollow.getCreateBy().equals(loginUser.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "你当前暂无该权限！");
        }
    }

    @Override
    public String followUser(User loginUser, Long id) {
        // 检查是否 已关注
        boolean flag = this.checkFollowStatus(loginUser, id);
        if (flag) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该用户已经被关注");
        }
        // 创建关注关系
        Follow follow = new Follow();
        //follow.setCreateBy(loginUser.getId()); // 这一步Mybatis plus 自动填充了
        follow.setFollowUserId(id);
        // 插入数据库
        followMapper.insert(follow);
        // todo 根据需要可自行实现添加 消息数量+1，存入message表
        //Message message = new Message();
        //message.setType(1);
        //message.setToId(id);
        //Long loginUserId = loginUser.getId();
        //User byId = userService.getById(loginUserId);
        //ThrowUtils.throwIfNull(byId);
        //// 伙伴用户@" + byId.getUserName() + "在" + 具体时间（2003-08-14 00:00:00) + "关注你啦~
        //message.setContent("伙伴用户@" + byId.getUserName() + "在" + DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss") + "关注你啦~");
        //message.setIsRead(0);
        // 缓存粉丝数量
        redisUtils.incr(RedisKeyConstant.MESSAGE_FANS_NUM_KEY + id, 1);
        // 返回操作结果
        return "关注成功";
    }

    @Override
    public String unfollowUser(User loginUser, Long id) {
        // 检查是否 已关注
        boolean flag = this.checkFollowStatus(loginUser, id);
        if (!flag) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "你尚未关注该用户");
        }
        // 删除关注关系
        LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Follow::getCreateBy, loginUser.getId());
        queryWrapper.eq(Follow::getFollowUserId, id);
        int affectedRows = followMapper.delete(queryWrapper);
        if (affectedRows == 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "取消关注失败");
        }
        // 移除缓存 -1
        redisUtils.decr(RedisKeyConstant.MESSAGE_FANS_NUM_KEY + id, 1);
        // 返回操作结果
        return "取消关注成功";
    }

    @Override
    public boolean checkFollowStatus(User loginUser, Long id) {
        LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Follow::getCreateBy, loginUser.getId());
        queryWrapper.eq(Follow::getFollowUserId, id);
        Follow existingFollow = followMapper.selectOne(queryWrapper);
        return existingFollow != null;
    }

}
