package icu.ydg.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.ydg.common.ErrorCode;
import icu.ydg.constant.PageConstant;
import icu.ydg.constant.RedisKeyConstant;
import icu.ydg.constant.UserConstant;
import icu.ydg.exception.BusinessException;
import icu.ydg.mapper.MessageMapper;
import icu.ydg.model.domain.*;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.message.MessageAddRequest;
import icu.ydg.model.dto.message.MessageQueryRequest;
import icu.ydg.model.dto.message.MessageUpdateRequest;
import icu.ydg.model.enums.message.MessageReadStatusEnums;
import icu.ydg.model.enums.message.MessageTypeEnums;
import icu.ydg.model.enums.sort.MessageSortFieldEnums;
import icu.ydg.model.vo.message.MessageVO;
import icu.ydg.model.vo.user.UserVO;
import icu.ydg.service.*;
import icu.ydg.utils.SqlUtils;
import icu.ydg.utils.ThrowUtils;
import icu.ydg.utils.redis.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 消息服务实现
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@Service
@Slf4j
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Resource
    private UserService userService;
    @Resource
    private MessageMapper messageMapper;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private PostService postService;
    @Resource
    private PostPraiseService postPraiseService;
    @Resource
    @Lazy
    private FollowService followService;

    /**
    * 添加消息
    *
    * @param messageAddRequest 发表评论添加请求
    * @param request               请求
    * @return long
    */
    @Override
    public long addMessage(MessageAddRequest messageAddRequest, HttpServletRequest request) {
        // todo 在此处将实体类和 DTO 进行转换
        Message message = new Message();
        BeanUtils.copyProperties(messageAddRequest, message);
        // 数据校验
        validMessage(message, true);
        // todo 填充值
        User loginUser = userService.getLoginUser(request);
        message.setCreateBy(loginUser.getId());
        // 写入数据库
        int insert = messageMapper.insert(message);
        ThrowUtils.throwIf(insert <= 0, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newMessageId = message.getId();
        // 返回新写入的数据 id
        return message.getId();
    }

    /**
    * 删除消息
    *
    * @param deleteRequest 删除请求
    * @param request       请求
    * @return long
    */
    @Override
    public long deleteMessage(IdRequest deleteRequest, HttpServletRequest request) {
        long id = deleteRequest.getId();
        onlyMeOrAdministratorCanDo(request, id);
        ThrowUtils.throwIf(messageMapper.deleteById(id) <= 0, ErrorCode.OPERATION_ERROR);
        return id;
    }

    /**
     * 校验数据
     *
     * @param message
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validMessage(Message message, boolean add) {
        ThrowUtils.throwIf(message == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值，自行修改为正确的属性
        //String title = message.getTitle();

        // 创建数据时，参数不能为空
        if (add) {
            // todo 补充参数不为空校验规则
            //ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR);
        }
        // 修改数据时，有参数则校验
        // todo 补充参数其他校验规则
        //if (StringUtils.isNotBlank(title)) {
        //    ThrowUtils.throwIf(title.length() > 80, ErrorCode.PARAMS_ERROR, "标题过长");
        //}
    }

    /**
     * 获取查询条件
     *
     * @param messageQueryRequest
     * @return
     */
    @Override
    public LambdaQueryWrapper<Message> getQueryWrapper(MessageQueryRequest messageQueryRequest) {
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        if (messageQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = messageQueryRequest.getId();
        Long notId = messageQueryRequest.getNotId();
        String title = messageQueryRequest.getTitle();
        String content = messageQueryRequest.getContent();
        String searchText = messageQueryRequest.getSearchText();
        String sortField = messageQueryRequest.getSortField();
        String sortOrder = messageQueryRequest.getSortOrder();
        Long userId = messageQueryRequest.getCreateBy();
        //List<String> tagList = messageQueryRequest.getTags();
        // todo 补充需要的查询条件
        // 从多字段中搜索
        //if (StringUtils.isNotBlank(searchText)) {
        //    // todo 需要拼接查询条件
        //    queryWrapper.and(qw -> qw.like(Message::getTitle, searchText).or().like(Message::getContent, searchText));
        //}
        // 模糊查询
        //queryWrapper.like(StringUtils.isNotBlank(title), Message::getTitle, title);
        //queryWrapper.like(StringUtils.isNotBlank(content), Message::getContent, content);
        // JSON 数组查询
        //if (CollUtil.isNotEmpty(tagList)) {
        //    for (String tag : tagList) {
        //        queryWrapper.like(Message::getTags, "\"" + tag + "\"");
        //    }
        //}
        // 精确查询
        //queryWrapper.ne(ObjectUtils.isNotEmpty(notId), Message::getId, notId);
        //queryWrapper.eq(ObjectUtils.isNotEmpty(id), Message::getId, id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), Message::getCreateBy, userId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(PageConstant.SORT_ORDER_ASC), isSortField(sortField));
        return queryWrapper;
    }

    /**
    * 是否为排序字段
    *
    * @param sortField 排序字段
    * @return {@code SFunction<Message, ?>}
    */
    private SFunction<Message, ?> isSortField(String sortField) {
        if (Objects.equals(sortField, "")) {
            sortField = MessageSortFieldEnums.UPDATE_TIME.name();
        }
        if (SqlUtils.validSortField(sortField)) {
            return MessageSortFieldEnums.fromString(sortField).map(MessageSortFieldEnums::getFieldGetter).orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "排序字段非法"));
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "排序字段无效，未知错误");
        }
    }

    /**
     * 获取消息封装
     *
     * @param idRequest
     * @param request
     * @return
     */
    @Override
    public MessageVO getMessageVO(IdRequest idRequest, HttpServletRequest request) {
        long id = idRequest.getId();
        // 查询数据库
        Message message = messageMapper.selectById(id);
        ThrowUtils.throwIf(message == null, ErrorCode.NOT_FOUND_ERROR);
        // 对象转封装类
        MessageVO messageVO = MessageVO.objToVo(message);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = message.getCreateBy();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        messageVO.setUser(userVO);
        return messageVO;
    }

    /**
    * “获取列表” 页
    *
    * @param messageQueryRequest
    * @return {@link Page }<{@link Message }>
    */
    @Override
    public Page<Message> getListPage(MessageQueryRequest messageQueryRequest) {
        long current = messageQueryRequest.getCurrent();
        long size = messageQueryRequest.getPageSize();
        if (size == 0L) {
            size = PageConstant.PAGE_SIZE;
        }
        if (current == 0L) {
            current = PageConstant.CURRENT_PAGE;
        }
        // 查询数据库
        return this.page(new Page<>(current, size), this.getQueryWrapper(messageQueryRequest));
    }

    /**
     * 分页获取消息封装
     *
     * @param messagePage
     * @param request
     * @return
     */
    @Override
    public Page<MessageVO> getMessageVOPage(Page<Message> messagePage, HttpServletRequest request) {
        List<Message> messageList = messagePage.getRecords();
        Page<MessageVO> messageVOPage = new Page<>(messagePage.getCurrent(), messagePage.getSize(), messagePage.getTotal());
        if (CollUtil.isEmpty(messageList)) {
            return messageVOPage;
        }
        // 对象列表 => 封装对象列表
        List<MessageVO> messageVOList = messageList.stream().map(message -> {
            return MessageVO.objToVo(message);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = messageList.stream().map(Message::getCreateBy).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        //Map<Long, Boolean> messageIdHasPraiseMap = new HashMap<>();
        //Map<Long, Boolean> messageIdHasCollectMap = new HashMap<>();
        //User loginUser = userService.getLoginUserPermitNull(request);
        //if (loginUser != null) {
        //    Set<Long> messageIdSet = messageList.stream().map(Message::getId).collect(Collectors.toSet());
        //    loginUser = userService.getLoginUser(request);
        //    // 获取点赞
        //    LambdaQueryWrapper<MessagePraise> messagePraiseQueryWrapper = new LambdaQueryWrapper<>();
        //    messagePraiseQueryWrapper.in(MessagePraise::getId, messageIdSet);
        //    messagePraiseQueryWrapper.eq(MessagePraise::getCreateBy, loginUser.getId());
        //    List<MessagePraise> messageMessagePraiseList = messageThumbMapper.selectList(messageThumbQueryWrapper);
        //    messageMessageThumbList.forEach(messageMessagePraise -> messageIdHasPraiseMap.put(messageMessagePraise.getMessageId(), true));
        //    // 获取收藏
        //    LambdaQueryWrapper<MessageCollect> messageCollectQueryWrapper = new LambdaQueryWrapper<>();
        //    messageCollectQueryWrapper.in(MessageCollect::getId, messageIdSet);
        //    messageCollectQueryWrapper.eq(MessageCollect::getCreateBy, loginUser.getId());
        //    List<MessageCollect> messageCollectList = messageCollectMapper.selectList(messageCollectQueryWrapper);
        //    messageCollectList.forEach(messageCollect -> messageIdHasCollectMap.put(messageCollect.getMessageId(), true));
        //}
        // 填充信息
        messageVOList.forEach(messageVO -> {
            Long userId = messageVO.getCreateBy();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            messageVO.setUser(userService.getUserVO(user));
        });
        // endregion

        messageVOPage.setRecords(messageVOList);
        return messageVOPage;
    }

    /**
    * 更新消息
    *
    * @param messageUpdateRequest 更新后请求
    * @param request           请求
    * @return long
    */
    @Override
    public long updateMessage(MessageUpdateRequest messageUpdateRequest, HttpServletRequest request) {
        // 本人和管理员可修改
        User user = userService.getLoginUser(request);
        if (!UserConstant.ADMIN_ROLE.equals(user.getUserRole()) && !Objects.equals(user.getId(), messageUpdateRequest.getCreateBy())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        long id = messageUpdateRequest.getId();
        // 获取数据
        Message oldMessage = messageMapper.selectById(id);
        ThrowUtils.throwIf(oldMessage == null, ErrorCode.NOT_FOUND_ERROR);
        // todo 设置值
        //oldMessage.setTitle(messageUpdateRequest.getTitle());
        //oldMessage.setContent(messageUpdateRequest.getContent());
        // 参数校验
        validMessage(oldMessage, false);
        // 更新
        messageMapper.updateById(oldMessage);
        return id;
    }

    /**
    * 处理分页和验证
    *
    * @param messageQueryRequest 消息查询请求
    * @param request          请求
    * @return {@code Page<MessageVO>}
    */
    @Override
    public Page<MessageVO> handlePaginationAndValidation(MessageQueryRequest messageQueryRequest, HttpServletRequest request) {
        long current = messageQueryRequest.getCurrent();
        long size = messageQueryRequest.getPageSize();
        if (size == 0L) {
            size = PageConstant.PAGE_SIZE;
        }
        if (current == 0L) {
            current = PageConstant.CURRENT_PAGE;
        }
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Message> messagePage = this.page(new Page<>(current, size), this.getQueryWrapper(messageQueryRequest));
        return this.getMessageVOPage(messagePage, request);
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
        Message oldMessage = messageMapper.selectById(id);
        ThrowUtils.throwIf(oldMessage == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可操作（这里假设"编辑"和"删除"操作的权限是一样的）
        if (!oldMessage.getCreateBy().equals(loginUser.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "你当前暂无该权限！");
        }
    }

    /**
     * 有新消息
     *
     * @param userId 用户id
     * @return boolean
     */
    @Override
    public boolean hasNewMessage(Long userId) {
        String postLike = RedisKeyConstant.MESSAGE_POST_COMMENT_NUM_KEY + userId;
        Boolean hasLike = redisUtils.hasKey(postLike);
        if (Boolean.TRUE.equals(hasLike)) {
            Integer likeNum = (Integer) redisUtils.get(postLike);
            assert likeNum != null;
            if (likeNum > 0) {
                return true;
            }
        }
        String friendApply = RedisKeyConstant.MESSAGE_FANS_NUM_KEY + userId;
        Boolean hasFollow = redisUtils.hasKey(friendApply);
        if (Boolean.TRUE.equals(hasFollow)) {
            Integer blogNum = (Integer) redisUtils.get(friendApply);
            assert blogNum != null;
            return blogNum > 0;
        }
        // todo 聊天消息
        Integer unReadPrivateNum = getUnReadPrivateNum(userId);
        return unReadPrivateNum > 0;
    }

    /**
     * 获取消息num
     *
     * @param userId 用户id
     * @return long
     */
    @Override
    public long getMessageNum(Long userId) {
        // 评论消息
        long postCommentNum = this.getPostCommentNum(userId);
        // 粉丝关注消息
        long fansNum = this.getFansNum(userId);
        // 私聊消息
        long privateNum = getUnReadPrivateNum(userId);
        // 队伍消息
        long teamNum = getUnReadTeamNum(userId);
        // 官方大厅消息
        long hallNum = (long) getUnReadHallNum(userId,false);
        return hallNum + privateNum + teamNum + postCommentNum + fansNum;
    }

    /**
     * 获得帖子好评num
     *
     * @param userId 用户id
     * @return long
     */
    @Override
    public long getPostPraiseNum(Long userId) {
        String likeNumKey = RedisKeyConstant.MESSAGE_POST_PRAISE_NUM_KEY + userId;
        Boolean hasLike = redisUtils.hasKey(likeNumKey);
        if (Boolean.TRUE.equals(hasLike)) {
            String likeNum = (String) redisUtils.get(likeNumKey);
            assert likeNum != null;
            return Long.parseLong(likeNum);
        } else {
            return 0;
        }
    }

    /**
     * 获取用户帖子
     *
     * @param userId 用户id
     * @return {@link List }<{@link Post }>
     */
    @Override
    public List<Post> getUserPost(Long userId) {
        // 获取点赞的帖子用户信息
        List<PostPraise> list = postPraiseService.list(
                new LambdaQueryWrapper<PostPraise>()
                        .eq(PostPraise::getCreateBy, userId)
                        .select(PostPraise::getPostId)
        );
        // 提取出所有的 postId
        List<Long> postIds = list.stream()
                .map(PostPraise::getPostId)
                .collect(Collectors.toList());

        // 如果没有点赞记录，直接返回空列表
        if (postIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 查询这些 postId 对应的帖子信息
        List<Post> postList = postService.list(new LambdaQueryWrapper<Post>().in(Post::getId, postIds));
        return postList;
    }

    /**
     * 获取好友num
     *
     * @param id id
     * @return long
     */
    @Override
    public long getFriendNum(Long id) {
        String likeNumKey = RedisKeyConstant.MESSAGE_FRIEND_APPLY_NUM_KEY + id;
        Boolean hasLike = redisUtils.hasKey(likeNumKey);
        if (Boolean.TRUE.equals(hasLike)) {
            String likeNum = (String) redisUtils.get(likeNumKey);
            assert likeNum != null;
            return Long.parseLong(likeNum);
        } else {
            return 0;
        }
    }

    /**
     * 获取用户vo
     *
     * @param id id
     * @return {@link List }<{@link UserVO }>
     */
    @Override
    public List<UserVO> getUserVO(Long id) {
        List<User> list = userService.list(
                new LambdaQueryWrapper<User>()
                        .eq(User::getId, id)
        );
        List<UserVO> userVOList = list.stream()
                .map(user -> {
                    UserVO userVO = new UserVO();
                    BeanUtils.copyProperties(user, userVO);  // 使用 BeanUtils 复制属性
                    return userVO;
                })
                .collect(Collectors.toList());
        return userVOList;
    }

    /**
     * 获取粉丝num
     *
     * @param id id
     * @return long
     */
    @Override
    public long getFansNum(Long id) {
        String likeNumKey = RedisKeyConstant.MESSAGE_FANS_NUM_KEY + id;
        Boolean hasLike = redisUtils.hasKey(likeNumKey);
        if (Boolean.TRUE.equals(hasLike)) {
            Integer likeNum = (Integer) redisUtils.get(likeNumKey);
            assert likeNum != null;
            return (likeNum);
        } else {
            return 0;
        }
    }

    @Override
    public List<UserVO> getFans(Long id) {
        List<Follow> followList = followService.list(
                new LambdaQueryWrapper<Follow>()
                        .eq(Follow::getFollowUserId, id)
        );
        // 提取所有 Follow 中的 createBy 用户ID
        List<Long> userIds = followList.stream()
                .map(Follow::getCreateBy)  // 获取每个 Follow 对象的 createBy 用户ID
                .collect(Collectors.toList());

        // 如果没有找到用户ID，直接返回空列表
        if (userIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 使用 userIds 查询 User 表，获取这些用户的详细信息
        List<User> userList = userService.list(
                new LambdaQueryWrapper<User>()
                        .in(User::getId, userIds)  // 根据用户ID列表进行查询
        );

        // 将 User 对象转换为 UserVO 对象
        return userList.stream()
                .map(user -> {
                    UserVO userVO = new UserVO();
                    BeanUtils.copyProperties(user, userVO);  // 使用 BeanUtils 复制属性
                    return userVO;
                })  // 创建 UserVO
                .collect(Collectors.toList());
    }

    /**
     * 获取帖子评论num
     *
     * @param id id
     * @return long
     */
    @Override
    public long getPostCommentNum(Long id) {
        String commentNumKey = RedisKeyConstant.MESSAGE_POST_COMMENT_NUM_KEY + id;
        Boolean hasComment = redisUtils.hasKey(commentNumKey);
        if (Boolean.TRUE.equals(hasComment)) {
            Integer commentNum = (Integer) redisUtils.get(commentNumKey);
            assert commentNum != null;
            return commentNum;
        } else {
            return 0;
        }
    }

    /**
     * 获取关注num
     *
     * @param id id
     * @return long
     */
    @Override
    public long getFollowNum(Long id) {
        String followNumKey = RedisKeyConstant.MESSAGE_FANS_NUM_KEY + id;
        Boolean hasFollow = redisUtils.hasKey(followNumKey);
        if (Boolean.TRUE.equals(hasFollow)) {
            Integer followNum = (Integer) redisUtils.get(followNumKey);
            assert followNum != null;
            return followNum;
        } else {
            return 0;
        }
    }

    /**
     * 获取私聊未读消息数量
     *
     * @param userId id
     * @return {@link Integer}
     */
    @Override
    public Integer getUnReadPrivateNum(Long userId) {
        LambdaQueryWrapper<Message> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatLambdaQueryWrapper.eq(Message::getToId, userId).eq(Message::getType, MessageTypeEnums.PRIVATE_CHAT.getValue())
                .eq(Message::getIsRead, MessageReadStatusEnums.UNREAD);
        return Math.toIntExact(this.count(chatLambdaQueryWrapper));
    }

    /**
     * 团队num
     *
     * @param userId 用户id
     * @return {@link Integer }
     */
    @Override
    public Integer getUnReadTeamNum(Long userId) {
        LambdaQueryWrapper<Message> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatLambdaQueryWrapper.eq(Message::getToId, userId).eq(Message::getType, MessageTypeEnums.TEAM_CHAT.getValue())
                .ne(Message::getCreateBy, userId)
                .eq(Message::getIsRead, MessageReadStatusEnums.UNREAD);
        return Math.toIntExact(this.count(chatLambdaQueryWrapper));
    }

    /**
     * 阅读私聊消息
     *
     * @param loginId  登录id
     * @param remoteId 遥远id
     * @return {@link Boolean}
     */
    @Override
    public Boolean readPrivateMessage(Long loginId, Long remoteId) {
        LambdaUpdateWrapper<Message> chatLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        chatLambdaUpdateWrapper.eq(Message::getCreateBy, remoteId)
                .eq(Message::getToId, loginId)
                .eq(Message::getType, MessageTypeEnums.PRIVATE_CHAT.getValue())
                .set(Message::getIsRead, MessageReadStatusEnums.READER.getValue());
        return this.update(chatLambdaUpdateWrapper);
    }

    /**
     * 阅读团队消息
     *
     * @param id     id
     * @param teamId 团队id
     * @return {@link Boolean }
     */
    @Override
    public Boolean readTeamMessage(Long id, Long teamId) {
        LambdaUpdateWrapper<Message> chatLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        chatLambdaUpdateWrapper.eq(Message::getTeamId, teamId)
                .ne(Message::getCreateBy, id)
                .eq(Message::getToId, id)
                .eq(Message::getType, MessageTypeEnums.TEAM_CHAT.getValue())
                .set(Message::getIsRead, MessageReadStatusEnums.READER.getValue());
        return this.update(chatLambdaUpdateWrapper);
    }

    /**
     * 读取hall num
     *
     * @param id            id
     * @param directApiCall 直接api调用
     * @return {@link Map }<{@link String }, {@link Object }>
     */
    @Override
    public Object getUnReadHallNum(Long id,Boolean directApiCall) {
        if (directApiCall) {
            Map<String, Object> map = new HashMap<>();
            LambdaQueryWrapper<Message> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
            chatLambdaQueryWrapper.eq(Message::getToId, id).eq(Message::getType, MessageTypeEnums.OFFICIAL_CHAT.getValue())
                    .ne(Message::getCreateBy, id)
                    .eq(Message::getIsRead, MessageReadStatusEnums.UNREAD);
            map.put("num", Math.toIntExact(this.count(chatLambdaQueryWrapper)));
            // 带上最新一条消息和最新时间发送的消息
            chatLambdaQueryWrapper.clear();
            chatLambdaQueryWrapper.eq(Message::getType, MessageTypeEnums.OFFICIAL_CHAT.getValue())
                    .orderByDesc(Message::getCreateTime)
                    .last("limit 1");
            Message message = this.getOne(chatLambdaQueryWrapper);
            if (message == null) {
                return map;
            }
            map.put("lastMessageTime", message.getCreateTime());
            map.put("lastMessage", message.getContent());
            return map;
        }
        LambdaQueryWrapper<Message> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatLambdaQueryWrapper.eq(Message::getToId, id).eq(Message::getType, MessageTypeEnums.OFFICIAL_CHAT.getValue())
                .ne(Message::getCreateBy, id)
                .eq(Message::getIsRead, MessageReadStatusEnums.UNREAD);
        return this.count(chatLambdaQueryWrapper);
    }

    /**
     * 阅读大厅信息
     *
     * @param id id
     * @return {@link Boolean }
     */
    @Override
    public Boolean readHallMessage(Long id) {
        LambdaUpdateWrapper<Message> chatLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        chatLambdaUpdateWrapper.eq(Message::getToId, id).eq(Message::getType, MessageTypeEnums.OFFICIAL_CHAT.getValue())
                .ne(Message::getCreateBy, id)
                .set(Message::getIsRead, MessageReadStatusEnums.READER.getValue());
        return this.update(chatLambdaUpdateWrapper);
    }

}
