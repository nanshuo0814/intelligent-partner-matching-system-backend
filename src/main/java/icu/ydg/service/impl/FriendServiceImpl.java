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
import icu.ydg.mapper.FriendMapper;
import icu.ydg.model.domain.Friend;
import icu.ydg.model.domain.Message;
import icu.ydg.model.domain.User;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.friend.FriendAddRequest;
import icu.ydg.model.dto.friend.FriendQueryRequest;
import icu.ydg.model.dto.friend.FriendUpdateRequest;
import icu.ydg.model.enums.friend.FriendStatusEnums;
import icu.ydg.model.enums.message.MessageTypeEnums;
import icu.ydg.model.enums.sort.FriendSortFieldEnums;
import icu.ydg.model.vo.friend.FriendVO;
import icu.ydg.model.vo.user.UserVO;
import icu.ydg.service.FriendService;
import icu.ydg.service.MessageService;
import icu.ydg.service.UserService;
import icu.ydg.utils.JsonUtils;
import icu.ydg.utils.SqlUtils;
import icu.ydg.utils.ThrowUtils;
import icu.ydg.utils.redis.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 好友服务实现
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@Service
@Slf4j
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements FriendService {

    @Resource
    private UserService userService;
    @Resource
    private FriendMapper friendMapper;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private MessageService messageService;

    /**
     * 添加好友
     *
     * @param friendAddRequest 发表评论添加请求
     * @param request          请求
     * @return long
     */
    @Override
    public long addFriend(FriendAddRequest friendAddRequest, HttpServletRequest request) {
        Friend friend = new Friend();
        BeanUtils.copyProperties(friendAddRequest, friend);
        // 数据校验
        validFriend(friend, true);
        User byId = userService.getById(friend.getReceiveId());
        ThrowUtils.throwIf(byId == null, ErrorCode.PARAMS_ERROR, "申请好友的用户不存在");
        ThrowUtils.throwIf(byId.getId().equals(friend.getCreateBy()), "不能添加自己为好友");
        // 不能重复添加
        LambdaQueryWrapper<Friend> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Friend::getReceiveId, friend.getReceiveId());
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        queryWrapper.eq(Friend::getCreateBy, loginUser.getId());
        Friend one = friendMapper.selectOne(queryWrapper);
        ThrowUtils.throwIf(one != null, ErrorCode.PARAMS_ERROR, "不能重复添加好友");
        // 写入数据库
        int insert = friendMapper.insert(friend);
        ThrowUtils.throwIf(insert <= 0, ErrorCode.OPERATION_ERROR);
        // 储存到缓存
        redisUtils.incr(RedisKeyConstant.MESSAGE_FRIEND_APPLY_NUM_KEY + friend.getReceiveId(), 1);
        // 储存消息
        Message message = new Message();
        message.setType(MessageTypeEnums.FRIEND_APPLY.getValue());
        message.setCreateBy(friend.getCreateBy());
        message.setToId(friend.getReceiveId());
        message.setContent(String.valueOf(friend.getId()));
        messageService.save(message);
        // 返回新写入的数据 id
        long newFriendId = friend.getId();
        // 返回新写入的数据 id
        return friend.getId();
    }

    /**
     * 删除好友
     *
     * @param deleteRequest 删除请求
     * @param request       请求
     * @return long
     */
    @Override
    public long deleteFriend(IdRequest deleteRequest, HttpServletRequest request) {
        long id = deleteRequest.getId();
        onlyMeOrAdministratorCanDo(request, id);
        ThrowUtils.throwIf(friendMapper.deleteById(id) <= 0, ErrorCode.OPERATION_ERROR);
        return id;
    }

    /**
     * 校验数据
     *
     * @param friend
     * @param add    对创建的数据进行校验
     */
    @Override
    public void validFriend(Friend friend, boolean add) {
        ThrowUtils.throwIf(friend == null, ErrorCode.PARAMS_ERROR);
        Long receiveId = friend.getReceiveId();
        ThrowUtils.throwIf(receiveId == null || receiveId <= 0, ErrorCode.PARAMS_ERROR, "申请好友的用户不存在");
        String remark = friend.getRemark();
        ThrowUtils.throwIf(remark.length() > 50, ErrorCode.PARAMS_ERROR, "备注过多");
        if (!add) {
            Long id = friend.getId();
            ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "申请好友的用户不存在");
            Integer status = friend.getStatus();
            ThrowUtils.throwIf(FriendStatusEnums.getEnumByValue(status) == null, "好友申请状态错误");
            // todo 展示不处理在这个
            //Integer isRead = friend.getIsRead();
        }
    }

    /**
     * 获取查询条件
     *
     * @param friendQueryRequest
     * @return
     */
    @Override
    public LambdaQueryWrapper<Friend> getQueryWrapper(FriendQueryRequest friendQueryRequest) {
        LambdaQueryWrapper<Friend> queryWrapper = new LambdaQueryWrapper<>();
        if (friendQueryRequest == null) {
            return queryWrapper;
        }
        Long id = friendQueryRequest.getId();
        String searchText = friendQueryRequest.getSearchText();
        String sortField = friendQueryRequest.getSortField();
        String sortOrder = friendQueryRequest.getSortOrder();
        Long userId = friendQueryRequest.getCreateBy();
        Long receiveId = friendQueryRequest.getReceiveId();
        Integer isRead = friendQueryRequest.getIsRead();
        Integer status = friendQueryRequest.getStatus();
        String remark = friendQueryRequest.getRemark();
        // 从多字段中搜索
        if (ObjectUtils.isNotEmpty(userId)) {
            queryWrapper.and(qw -> qw.like(Friend::getCreateBy, userId).or().like(Friend::getReceiveId, userId));
        }
        // 模糊查询
        queryWrapper.like(ObjectUtils.isNotEmpty(remark), Friend::getRemark, remark);
        // 精确查询
        queryWrapper.eq(Friend::getStatus, status == null ? 0 : status);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), Friend::getId, id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(isRead), Friend::getIsRead, isRead);
        //queryWrapper.eq(ObjectUtils.isNotEmpty(userId), Friend::getCreateBy, userId);
        //queryWrapper.eq(ObjectUtils.isNotEmpty(receiveId), Friend::getReceiveId, receiveId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(PageConstant.SORT_ORDER_ASC), isSortField(sortField));
        return queryWrapper;
    }

    /**
     * 是否为排序字段
     *
     * @param sortField 排序字段
     * @return {@code SFunction<Friend, ?>}
     */
    private SFunction<Friend, ?> isSortField(String sortField) {
        if (Objects.equals(sortField, "")) {
            sortField = FriendSortFieldEnums.UPDATE_TIME.name();
        }
        if (SqlUtils.validSortField(sortField)) {
            return FriendSortFieldEnums.fromString(sortField).map(FriendSortFieldEnums::getFieldGetter).orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "排序字段非法"));
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "排序字段无效，未知错误");
        }
    }

    /**
     * 获取好友封装
     *
     * @param idRequest
     * @param request
     * @return
     */
    @Override
    public FriendVO getFriendVO(IdRequest idRequest, HttpServletRequest request) {
        long id = idRequest.getId();
        // 查询数据库
        Friend friend = friendMapper.selectById(id);
        ThrowUtils.throwIf(friend == null, ErrorCode.NOT_FOUND_ERROR);
        // 对象转封装类
        FriendVO friendVO = FriendVO.objToVo(friend);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = friend.getCreateBy();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        friendVO.setUser(userVO);
        return friendVO;
    }

    /**
     * “获取列表” 页
     *
     * @param friendQueryRequest
     * @return {@link Page }<{@link Friend }>
     */
    @Override
    public Page<Friend> getListPage(FriendQueryRequest friendQueryRequest) {
        long current = friendQueryRequest.getCurrent();
        long size = friendQueryRequest.getPageSize();
        if (size == 0L) {
            size = PageConstant.PAGE_SIZE;
        }
        if (current == 0L) {
            current = PageConstant.CURRENT_PAGE;
        }
        // 查询数据库
        return this.page(new Page<>(current, size), this.getQueryWrapper(friendQueryRequest));
    }

    /**
     * 分页获取好友封装
     *
     * @param friendPage
     * @param request
     * @return
     */
    @Override
    public Page<FriendVO> getFriendVOPage(Page<Friend> friendPage, HttpServletRequest request) {
        List<Friend> friendList = friendPage.getRecords();
        Page<FriendVO> friendVOPage = new Page<>(friendPage.getCurrent(), friendPage.getSize(), friendPage.getTotal());
        if (CollUtil.isEmpty(friendList)) {
            return friendVOPage;
        }
        // 对象列表 => 封装对象列表
        List<FriendVO> friendVOList = friendList.stream().map(friend -> {
            return FriendVO.objToVo(friend);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = friendList.stream().map(Friend::getCreateBy).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        //Map<Long, Boolean> friendIdHasPraiseMap = new HashMap<>();
        //Map<Long, Boolean> friendIdHasCollectMap = new HashMap<>();
        //User loginUser = userService.getLoginUserPermitNull(request);
        //if (loginUser != null) {
        //    Set<Long> friendIdSet = friendList.stream().map(Friend::getId).collect(Collectors.toSet());
        //    loginUser = userService.getLoginUser(request);
        //    // 获取点赞
        //    LambdaQueryWrapper<FriendPraise> friendPraiseQueryWrapper = new LambdaQueryWrapper<>();
        //    friendPraiseQueryWrapper.in(FriendPraise::getId, friendIdSet);
        //    friendPraiseQueryWrapper.eq(FriendPraise::getCreateBy, loginUser.getId());
        //    List<FriendPraise> friendFriendPraiseList = friendThumbMapper.selectList(friendThumbQueryWrapper);
        //    friendFriendThumbList.forEach(friendFriendPraise -> friendIdHasPraiseMap.put(friendFriendPraise.getFriendId(), true));
        //    // 获取收藏
        //    LambdaQueryWrapper<FriendCollect> friendCollectQueryWrapper = new LambdaQueryWrapper<>();
        //    friendCollectQueryWrapper.in(FriendCollect::getId, friendIdSet);
        //    friendCollectQueryWrapper.eq(FriendCollect::getCreateBy, loginUser.getId());
        //    List<FriendCollect> friendCollectList = friendCollectMapper.selectList(friendCollectQueryWrapper);
        //    friendCollectList.forEach(friendCollect -> friendIdHasCollectMap.put(friendCollect.getFriendId(), true));
        //}
        // 填充信息
        friendVOList.forEach(friendVO -> {
            Long userId = friendVO.getCreateBy();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            friendVO.setUser(userService.getUserVO(user));
        });
        // endregion

        friendVOPage.setRecords(friendVOList);
        return friendVOPage;
    }

    /**
     * 更新好友
     *
     * @param friendUpdateRequest 更新后请求
     * @param request             请求
     * @return long
     */
    @Override
    public long updateFriend(FriendUpdateRequest friendUpdateRequest, HttpServletRequest request) {
        // 本人和管理员可修改
        User user = userService.getLoginUser(request);
        boolean receiveId = Objects.equals(user.getId(), friendUpdateRequest.getReceiveId());
        if (!UserConstant.ADMIN_ROLE.equals(user.getUserRole()) && !Objects.equals(user.getId(), friendUpdateRequest.getCreateBy()) && !receiveId) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        long id = friendUpdateRequest.getId();
        // 获取数据
        Friend oldFriend = friendMapper.selectById(id);
        ThrowUtils.throwIf(oldFriend == null, ErrorCode.NOT_FOUND_ERROR);
        if (!receiveId) {
            // 不能让被申请好友的用户修改申请好友的用户的备注
            oldFriend.setRemark(friendUpdateRequest.getRemark());
        } else {
            // 如果是被申请加好友的人更新status
            oldFriend.setIsRead(1);
            oldFriend.setStatus(friendUpdateRequest.getStatus());
            // 被申请的用户
            Long applyForUser = user.getId();
            User user1 = userService.getById(applyForUser);

            // 获取当前的好友列表，解析为 List<Long>
            List<Long> friendList1 = parseFriendList(user1.getFriendList());
            // 只有当新的好友 ID 不存在时，才加入
            if (!friendList1.contains(friendUpdateRequest.getCreateBy())) {
                friendList1.add(friendUpdateRequest.getCreateBy());
            } else {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "已经是好友了");
            }
            // 更新好友列表
            user1.setFriendList(JsonUtils.objToJson(friendList1));
            userService.updateById(user1);

            // 申请的用户
            Long applyUser = friendUpdateRequest.getCreateBy();
            User user2 = userService.getById(applyUser);

            // 获取当前的好友列表，解析为 List<Long>
            List<Long> friendList2 = parseFriendList(user2.getFriendList());
            // 只有当新的好友 ID 不存在时，才加入
            if (!friendList2.contains(user.getId())) {
                friendList2.add(user.getId());
            } else {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "已经是好友了");
            }
            // 更新好友列表
            user2.setFriendList(JsonUtils.objToJson(friendList2));
            userService.updateById(user2);
        }
        oldFriend.setReceiveId(friendUpdateRequest.getReceiveId());
        // 参数校验
        validFriend(oldFriend, false);
        // 更新
        friendMapper.updateById(oldFriend);
        return id;
    }


    /**
     * 解析字符串类型的好友列表，转换为 List<Long> 格式
     *
     * @param friendListStr 好友列表str
     * @return {@link List }<{@link Long }>
     */
    private List<Long> parseFriendList(String friendListStr) {
        if (friendListStr == null || friendListStr.isEmpty()) {
            return new ArrayList<>();  // 如果没有好友列表，则返回空列表
        }
        // 使用 fastjson 解析成 List<Long>
        return JsonUtils.jsonToList(friendListStr, Long.class);
    }

    /**
     * 处理分页和验证
     *
     * @param friendQueryRequest 好友查询请求
     * @param request            请求
     * @return {@code Page<FriendVO>}
     */
    @Override
    public Page<FriendVO> handlePaginationAndValidation(FriendQueryRequest friendQueryRequest, HttpServletRequest request) {
        long current = friendQueryRequest.getCurrent();
        long size = friendQueryRequest.getPageSize();
        if (size == 0L) {
            size = PageConstant.PAGE_SIZE;
        }
        if (current == 0L) {
            current = PageConstant.CURRENT_PAGE;
        }
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Friend> friendPage = this.page(new Page<>(current, size), this.getQueryWrapper(friendQueryRequest));
        return this.getFriendVOPage(friendPage, request);
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
        Friend oldFriend = friendMapper.selectById(id);
        ThrowUtils.throwIf(oldFriend == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可操作（这里假设"编辑"和"删除"操作的权限是一样的）
        if (!oldFriend.getCreateBy().equals(loginUser.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "你当前暂无该权限！");
        }
    }

}
