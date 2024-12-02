package icu.ydg.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.ydg.common.ErrorCode;
import icu.ydg.constant.PageConstant;
import icu.ydg.constant.RedisKeyConstant;
import icu.ydg.constant.UserConstant;
import icu.ydg.exception.BusinessException;
import icu.ydg.mapper.ChatMapper;
import icu.ydg.model.domain.Chat;
import icu.ydg.model.domain.Message;
import icu.ydg.model.domain.Team;
import icu.ydg.model.domain.User;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.chat.*;
import icu.ydg.model.enums.message.MessageReadStatusEnums;
import icu.ydg.model.enums.message.MessageTypeEnums;
import icu.ydg.model.enums.sort.ChatSortFieldEnums;
import icu.ydg.model.vo.chat.ChatMessageVO;
import icu.ydg.model.vo.chat.ChatVO;
import icu.ydg.model.vo.team.TeamVO;
import icu.ydg.model.vo.user.UserVO;
import icu.ydg.model.vo.ws.WebSocketVO;
import icu.ydg.service.*;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 聊天服务实现
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@Service
@Slf4j
public class ChatServiceImpl extends ServiceImpl<ChatMapper, Chat> implements ChatService {

    @Resource
    private UserService userService;
    @Resource
    private ChatMapper chatMapper;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private TeamService teamService;
    @Resource
    private UserTeamService userTeamService;
    @Resource
    private MessageService messageService;
    // todo 如果后续需要点赞或收藏可自行添加
    //@Resource
    //private ChatPraiseMapper chatPraiseMapper;
    //@Resource
    //private ChatCollectMapper chatCollectMapper;

    /**
     * 添加聊天
     *
     * @param chatAddRequest 发表评论添加请求
     * @param request        请求
     * @return long
     */
    @Override
    public long addChat(ChatAddRequest chatAddRequest, HttpServletRequest request) {
        // todo 在此处将实体类和 DTO 进行转换
        Chat chat = new Chat();
        BeanUtils.copyProperties(chatAddRequest, chat);
        // 数据校验
        validChat(chat, true);
        // todo 填充值
        User loginUser = userService.getLoginUser(request);
        chat.setCreateBy(loginUser.getId());
        // 写入数据库
        int insert = chatMapper.insert(chat);
        ThrowUtils.throwIf(insert <= 0, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newChatId = chat.getId();
        // 返回新写入的数据 id
        return chat.getId();
    }

    /**
     * 删除聊天
     *
     * @param deleteRequest 删除请求
     * @param request       请求
     * @return long
     */
    @Override
    public long deleteChat(IdRequest deleteRequest, HttpServletRequest request) {
        long id = deleteRequest.getId();
        onlyMeOrAdministratorCanDo(request, id);
        ThrowUtils.throwIf(chatMapper.deleteById(id) <= 0, ErrorCode.OPERATION_ERROR);
        return id;
    }

    /**
     * 校验数据
     *
     * @param chat
     * @param add  对创建的数据进行校验
     */
    @Override
    public void validChat(Chat chat, boolean add) {
        ThrowUtils.throwIf(chat == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值，自行修改为正确的属性
        //String title = chat.getTitle();

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
     * @param chatQueryRequest
     * @return
     */
    @Override
    public LambdaQueryWrapper<Chat> getQueryWrapper(ChatQueryRequest chatQueryRequest) {
        LambdaQueryWrapper<Chat> queryWrapper = new LambdaQueryWrapper<>();
        if (chatQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = chatQueryRequest.getId();
        Long notId = chatQueryRequest.getNotId();
        String title = chatQueryRequest.getTitle();
        String content = chatQueryRequest.getContent();
        String searchText = chatQueryRequest.getSearchText();
        String sortField = chatQueryRequest.getSortField();
        String sortOrder = chatQueryRequest.getSortOrder();
        Long userId = chatQueryRequest.getCreateBy();
        //List<String> tagList = chatQueryRequest.getTags();
        // todo 补充需要的查询条件
        // 从多字段中搜索
        //if (StringUtils.isNotBlank(searchText)) {
        //    // todo 需要拼接查询条件
        //    queryWrapper.and(qw -> qw.like(Chat::getTitle, searchText).or().like(Chat::getContent, searchText));
        //}
        // 模糊查询
        //queryWrapper.like(StringUtils.isNotBlank(title), Chat::getTitle, title);
        //queryWrapper.like(StringUtils.isNotBlank(content), Chat::getContent, content);
        // JSON 数组查询
        //if (CollUtil.isNotEmpty(tagList)) {
        //    for (String tag : tagList) {
        //        queryWrapper.like(Chat::getTags, "\"" + tag + "\"");
        //    }
        //}
        // 精确查询
        //queryWrapper.ne(ObjectUtils.isNotEmpty(notId), Chat::getId, notId);
        //queryWrapper.eq(ObjectUtils.isNotEmpty(id), Chat::getId, id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), Chat::getCreateBy, userId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(PageConstant.SORT_ORDER_ASC), isSortField(sortField));
        return queryWrapper;
    }

    /**
     * 是否为排序字段
     *
     * @param sortField 排序字段
     * @return {@code SFunction<Chat, ?>}
     */
    private SFunction<Chat, ?> isSortField(String sortField) {
        if (Objects.equals(sortField, "")) {
            sortField = ChatSortFieldEnums.UPDATE_TIME.name();
        }
        if (SqlUtils.validSortField(sortField)) {
            return ChatSortFieldEnums.fromString(sortField).map(ChatSortFieldEnums::getFieldGetter).orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "排序字段非法"));
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "排序字段无效，未知错误");
        }
    }

    /**
     * 获取聊天封装
     *
     * @param idRequest
     * @param request
     * @return
     */
    @Override
    public ChatVO getChatVO(IdRequest idRequest, HttpServletRequest request) {
        long id = idRequest.getId();
        // 查询数据库
        Chat chat = chatMapper.selectById(id);
        ThrowUtils.throwIf(chat == null, ErrorCode.NOT_FOUND_ERROR);
        // 对象转封装类
        ChatVO chatVO = ChatVO.objToVo(chat);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = chat.getCreateBy();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        chatVO.setUser(userVO);
        return chatVO;
    }

    /**
     * “获取列表” 页
     *
     * @param chatQueryRequest
     * @return {@link Page }<{@link Chat }>
     */
    @Override
    public Page<Chat> getListPage(ChatQueryRequest chatQueryRequest) {
        long current = chatQueryRequest.getCurrent();
        long size = chatQueryRequest.getPageSize();
        if (size == 0L) {
            size = PageConstant.PAGE_SIZE;
        }
        if (current == 0L) {
            current = PageConstant.CURRENT_PAGE;
        }
        // 查询数据库
        return this.page(new Page<>(current, size), this.getQueryWrapper(chatQueryRequest));
    }

    /**
     * 分页获取聊天封装
     *
     * @param chatPage
     * @param request
     * @return
     */
    @Override
    public Page<ChatVO> getChatVOPage(Page<Chat> chatPage, HttpServletRequest request) {
        List<Chat> chatList = chatPage.getRecords();
        Page<ChatVO> chatVOPage = new Page<>(chatPage.getCurrent(), chatPage.getSize(), chatPage.getTotal());
        if (CollUtil.isEmpty(chatList)) {
            return chatVOPage;
        }
        // 对象列表 => 封装对象列表
        List<ChatVO> chatVOList = chatList.stream().map(chat -> {
            return ChatVO.objToVo(chat);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = chatList.stream().map(Chat::getCreateBy).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        //Map<Long, Boolean> chatIdHasPraiseMap = new HashMap<>();
        //Map<Long, Boolean> chatIdHasCollectMap = new HashMap<>();
        //User loginUser = userService.getLoginUserPermitNull(request);
        //if (loginUser != null) {
        //    Set<Long> chatIdSet = chatList.stream().map(Chat::getId).collect(Collectors.toSet());
        //    loginUser = userService.getLoginUser(request);
        //    // 获取点赞
        //    LambdaQueryWrapper<ChatPraise> chatPraiseQueryWrapper = new LambdaQueryWrapper<>();
        //    chatPraiseQueryWrapper.in(ChatPraise::getId, chatIdSet);
        //    chatPraiseQueryWrapper.eq(ChatPraise::getCreateBy, loginUser.getId());
        //    List<ChatPraise> chatChatPraiseList = chatThumbMapper.selectList(chatThumbQueryWrapper);
        //    chatChatThumbList.forEach(chatChatPraise -> chatIdHasPraiseMap.put(chatChatPraise.getChatId(), true));
        //    // 获取收藏
        //    LambdaQueryWrapper<ChatCollect> chatCollectQueryWrapper = new LambdaQueryWrapper<>();
        //    chatCollectQueryWrapper.in(ChatCollect::getId, chatIdSet);
        //    chatCollectQueryWrapper.eq(ChatCollect::getCreateBy, loginUser.getId());
        //    List<ChatCollect> chatCollectList = chatCollectMapper.selectList(chatCollectQueryWrapper);
        //    chatCollectList.forEach(chatCollect -> chatIdHasCollectMap.put(chatCollect.getChatId(), true));
        //}
        // 填充信息
        chatVOList.forEach(chatVO -> {
            Long userId = chatVO.getCreateBy();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            chatVO.setUser(userService.getUserVO(user));
        });
        // endregion

        chatVOPage.setRecords(chatVOList);
        return chatVOPage;
    }

    /**
     * 更新聊天
     *
     * @param chatUpdateRequest 更新后请求
     * @param request           请求
     * @return long
     */
    @Override
    public long updateChat(ChatUpdateRequest chatUpdateRequest, HttpServletRequest request) {
        // 本人和管理员可修改
        User user = userService.getLoginUser(request);
        if (!UserConstant.ADMIN_ROLE.equals(user.getUserRole()) && !Objects.equals(user.getId(), chatUpdateRequest.getCreateBy())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        long id = chatUpdateRequest.getId();
        // 获取数据
        Chat oldChat = chatMapper.selectById(id);
        ThrowUtils.throwIf(oldChat == null, ErrorCode.NOT_FOUND_ERROR);
        // todo 设置值
        //oldChat.setTitle(chatUpdateRequest.getTitle());
        //oldChat.setContent(chatUpdateRequest.getContent());
        // 参数校验
        validChat(oldChat, false);
        // 更新
        chatMapper.updateById(oldChat);
        return id;
    }

    /**
     * 处理分页和验证
     *
     * @param chatQueryRequest 聊天查询请求
     * @param request          请求
     * @return {@code Page<ChatVO>}
     */
    @Override
    public Page<ChatVO> handlePaginationAndValidation(ChatQueryRequest chatQueryRequest, HttpServletRequest request) {
        long current = chatQueryRequest.getCurrent();
        long size = chatQueryRequest.getPageSize();
        if (size == 0L) {
            size = PageConstant.PAGE_SIZE;
        }
        if (current == 0L) {
            current = PageConstant.CURRENT_PAGE;
        }
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chat> chatPage = this.page(new Page<>(current, size), this.getQueryWrapper(chatQueryRequest));
        return this.getChatVOPage(chatPage, request);
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
        Chat oldChat = chatMapper.selectById(id);
        ThrowUtils.throwIf(oldChat == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可操作（这里假设"编辑"和"删除"操作的权限是一样的）
        if (!oldChat.getCreateBy().equals(loginUser.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "你当前暂无该权限！");
        }
    }

    /**
     * 获取聊天
     *
     * @param chatRequest 聊天请求
     * @param chatType    聊天类型
     * @param loginUser   登录用户
     * @return {@link List }<{@link ChatMessageVO }>
     */
    @Override
    public List<ChatMessageVO> getChat(ChatRequest chatRequest, int chatType, User loginUser) {
        Long toId = chatRequest.getToId();
        if (toId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<ChatMessageVO> chatRecords = getCache(RedisKeyConstant.CACHE_CHAT_PRIVATE, loginUser.getId() + "-" + String.valueOf(toId));
        if (chatRecords != null) {
            saveCache(RedisKeyConstant.CACHE_CHAT_PRIVATE, loginUser.getId() + "-" + String.valueOf(toId), chatRecords);
            return chatRecords;
        }
        LambdaQueryWrapper<Chat> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatLambdaQueryWrapper.
                and(privateChat -> privateChat.eq(Chat::getCreateBy, loginUser.getId()).eq(Chat::getToId, toId)
                        .or().
                        eq(Chat::getToId, loginUser.getId()).eq(Chat::getCreateBy, toId)
                ).eq(Chat::getChatType, chatType);
        // 两方共有聊天
        List<Chat> list = this.list(chatLambdaQueryWrapper);
        List<ChatMessageVO> chatMessageVOList = list.stream().map(chat -> {
            ChatMessageVO chatMessageVo = chatResult(loginUser.getId(),
                    toId, chat.getText(), chatType,
                    chat.getCreateTime());
            if (chat.getCreateBy().equals(loginUser.getId())) {
                chatMessageVo.setIsMy(true);
            }
            return chatMessageVo;
        }).collect(Collectors.toList());
        saveCache(RedisKeyConstant.CACHE_CHAT_PRIVATE, loginUser.getId() + "-" + String.valueOf(toId), chatMessageVOList);
        return chatMessageVOList;
    }

    /**
     * 聊天结果
     *
     * @param userId     用户id
     * @param toId       到id
     * @param text       文本
     * @param chatType   聊天类型
     * @param createTime 创建时间
     * @return {@link ChatMessageVO}
     */
    @Override
    public ChatMessageVO chatResult(Long userId, Long toId, String text, Integer chatType, Date createTime) {
        ChatMessageVO chatMessageVo = new ChatMessageVO();
        User fromUser = userService.getById(userId);
        User toUser = userService.getById(toId);
        WebSocketVO fromWebSocketVo = new WebSocketVO();
        WebSocketVO toWebSocketVo = new WebSocketVO();
        BeanUtils.copyProperties(fromUser, fromWebSocketVo);
        BeanUtils.copyProperties(toUser, toWebSocketVo);
        chatMessageVo.setFromUser(fromWebSocketVo);
        chatMessageVo.setToUser(toWebSocketVo);
        chatMessageVo.setChatType(chatType);
        chatMessageVo.setText(text);
        chatMessageVo.setCreateTime(DateUtil.format(createTime, "yyyy-MM-dd HH:mm:ss"));
        return chatMessageVo;
    }

    /**
     * 获取私聊列表
     *
     * @param userId id
     * @return {@link List}<{@link UserVO}>
     */
    @Override
    public List<PrivateChatVO> getPrivateList(Long userId) {
        LambdaQueryWrapper<Chat> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatLambdaQueryWrapper.eq(Chat::getCreateBy, userId).eq(Chat::getChatType, 3);
        List<Chat> mySend = this.list(chatLambdaQueryWrapper);
        HashSet<Long> userIdSet = new HashSet<>();
        mySend.forEach((chat) -> {
            Long toId = chat.getToId();
            userIdSet.add(toId);
        });
        chatLambdaQueryWrapper.clear();
        chatLambdaQueryWrapper.eq(Chat::getToId, userId).eq(Chat::getChatType, 3);
        List<Chat> myReceive = this.list(chatLambdaQueryWrapper);
        myReceive.forEach((chat) -> {
            Long fromId = chat.getCreateBy();
            userIdSet.add(fromId);
        });
        List<User> userList = userService.listByIds(userIdSet);
        return userList.stream().map((user) -> {
            PrivateChatVO privateChatVO = new PrivateChatVO();
            // 查询用户
            User byId = userService.getById(user.getId());
            UserVO userVO = UserVO.objToVo(byId);
            privateChatVO.setUser(userVO);
            Pair<String, Date> pair = getPrivateLastMessage(userId, user.getId());
            privateChatVO.setLastMessage(pair.getKey());
            privateChatVO.setLastMessageDate(pair.getValue());
            privateChatVO.setUnReadNum(getPrivateChatUnreadNum(userId, user.getId(), MessageTypeEnums.PRIVATE_CHAT.getValue()));
            return privateChatVO;
        }).sorted().collect(Collectors.toList());
    }

    /**
     * 阅读私聊消息
     *
     * @param loginId  登录id
     * @param remoteId 遥远id
     * @return {@link Boolean}
     */
    //@Override
    //public Boolean readPrivateMessage(Long loginId, Long remoteId) {
    //    LambdaUpdateWrapper<Chat> chatLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
    //    chatLambdaUpdateWrapper.eq(Chat::getCreateBy, remoteId)
    //            .eq(Chat::getToId, loginId)
    //            .eq(Chat::getChatType, 3)
    //            .set(Chat::getIsRead, 1);
    //    return this.update(chatLambdaUpdateWrapper);
    //}

    /**
     * 获取团队聊天
     *
     * @param chatRequest 聊天请求
     * @param chatType    聊天类型
     * @param loginUser   登录用户
     * @return {@link List}<{@link ChatMessageVO}>
     */
    @Override
    public List<ChatMessageVO> getTeamChat(ChatRequest chatRequest, int chatType, User loginUser) {
        Long teamId = chatRequest.getTeamId();
        if (teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求有误");
        }
        List<ChatMessageVO> chatRecords = getCache(RedisKeyConstant.CACHE_CHAT_TEAM, String.valueOf(teamId));
        if (chatRecords != null) {
            List<ChatMessageVO> chatMessageVOS = checkIsMyMessage(loginUser, chatRecords);
            saveCache(RedisKeyConstant.CACHE_CHAT_TEAM, String.valueOf(teamId), chatMessageVOS);
            return chatMessageVOS;
        }
        Team team = teamService.getById(teamId);
        LambdaQueryWrapper<Chat> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatLambdaQueryWrapper.eq(Chat::getChatType, chatType).eq(Chat::getTeamId, teamId);
        List<ChatMessageVO> chatMessageVOS = returnMessage(loginUser, team.getCreateBy(), chatLambdaQueryWrapper);
        saveCache(RedisKeyConstant.CACHE_CHAT_TEAM, String.valueOf(teamId), chatMessageVOS);
        return chatMessageVOS;
    }

    /**
     * 获取私聊未读消息数量
     *
     * @param userId id
     * @return {@link Integer}
     */
    //@Override
    //public Integer getUnReadPrivateNum(Long userId) {
    //    LambdaQueryWrapper<Chat> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
    //    chatLambdaQueryWrapper.eq(Chat::getToId, userId).eq(Chat::getChatType, 3)
    //            .eq(Chat::getIsRead, 2);
    //    return Math.toIntExact(this.count(chatLambdaQueryWrapper));
    //}

    /**
     * 删除密钥
     *
     * @param key 钥匙
     * @param id  id
     */
    @Override
    public void deleteKey(String key, String id) {
        if (key.equals(RedisKeyConstant.CACHE_CHAT_HALL)) {
            redisUtils.del(key);
        } else {
            redisUtils.del(key + id);
        }
    }

    /**
     * 获取团队列表
     *
     * @param userId 用户id
     * @return {@link List }<{@link PrivateChatVO }>
     */
    @Override
    public List<PrivateChatVO> getTeamList(Long userId) {
        // 根据userId获取到用户加入的队伍
        List<Long> joinedTeamIds = userTeamService.getJoinedTeamIdsByUserId(userId);
        LambdaQueryWrapper<Chat> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatLambdaQueryWrapper.in(Chat::getTeamId, joinedTeamIds).eq(Chat::getChatType, 4);
        List<Chat> chatList = this.list(chatLambdaQueryWrapper);
        HashSet<Long> teamIdSet = new HashSet<>();
        chatList.forEach((chat) -> {
            Long teamId = chat.getTeamId();
            teamIdSet.add(teamId);
            if (teamIdSet.contains(teamId)) {
                teamIdSet.add(teamId);
                return;
            }
        });
        List<Team> teamList = teamService.listByIds(teamIdSet);
        return teamList.stream().map((team) -> {
            PrivateChatVO privateChatVO = new PrivateChatVO();
            privateChatVO.setTeam(TeamVO.objToVo(team));
            Pair<String, Date> pair = getTeamLastMessage(team.getId());
            privateChatVO.setLastMessage(pair.getKey());
            privateChatVO.setLastMessageDate(pair.getValue());
            privateChatVO.setUnReadNum(getTeamChatUnreadNum(userId, team.getId(), MessageTypeEnums.TEAM_CHAT.getValue()));
            return privateChatVO;
        }).sorted().collect(Collectors.toList());
    }

    /**
     * 获取团队聊天未读num
     *
     * @param userId 用户id
     * @param id     id
     * @param type   类型
     * @return {@link Integer }
     */
    private Integer getTeamChatUnreadNum(Long userId, Long id, Integer type) {
        LambdaQueryWrapper<Message> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatLambdaQueryWrapper.eq(Message::getToId, userId).eq(Message::getTeamId, id)
                .eq(Message::getType, type)
                .ne(Message::getCreateBy, userId)
                .eq(Message::getIsRead, MessageReadStatusEnums.UNREAD.getValue());
        return Math.toIntExact(messageService.count(chatLambdaQueryWrapper));
    }

    /**
     * 团队num
     *
     * @param userId 用户id
     * @return {@link Integer }
     */
    //@Override
    //public Integer getUnReadTeamNum(Long userId) {
    //    LambdaQueryWrapper<Chat> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
    //    chatLambdaQueryWrapper.eq(Chat::getToId, userId).eq(Chat::getChatType, 4)
    //            .eq(Chat::getIsRead, 2);
    //    return Math.toIntExact(this.count(chatLambdaQueryWrapper));
    //}

    /**
     * 获取团队最后一条消息
     *
     * @param teamId 团队id
     * @return {@link Pair }<{@link String }, {@link Date }>
     */
    private Pair<String, Date> getTeamLastMessage(Long teamId) {
        LambdaQueryWrapper<Chat> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatLambdaQueryWrapper.eq(Chat::getChatType, 4)
                .eq(Chat::getTeamId, teamId)
                .orderByDesc(Chat::getCreateTime)
                .last("LIMIT 1"); // 确保只获取一条记录
        Chat chat = this.getOne(chatLambdaQueryWrapper);
        // 空值检查，避免空指针异常
        if (chat == null) {
            return Pair.of(null, null); // 或者你可以返回一个适当的默认值
        }
        return Pair.of(chat.getText(), chat.getCreateTime());
    }

    /**
     * 获得大厅聊天
     *
     * @param chatType  聊天类型
     * @param loginUser 登录用户
     * @return {@link List}<{@link ChatMessageVO}>
     */
    @Override
    public List<ChatMessageVO> getHallChat(int chatType, User loginUser) {
        List<ChatMessageVO> chatRecords = getCache(RedisKeyConstant.CACHE_CHAT_HALL, String.valueOf(loginUser.getId()));
        if (chatRecords != null) {
            List<ChatMessageVO> chatMessageVOS = checkIsMyMessage(loginUser, chatRecords);
            saveCache(RedisKeyConstant.CACHE_CHAT_HALL, String.valueOf(loginUser.getId()), chatMessageVOS);
            return chatMessageVOS;
        }
        LambdaQueryWrapper<Chat> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatLambdaQueryWrapper.eq(Chat::getChatType, chatType);
        List<ChatMessageVO> chatMessageVOS = returnMessage(loginUser, null, chatLambdaQueryWrapper);
        saveCache(RedisKeyConstant.CACHE_CHAT_HALL, String.valueOf(loginUser.getId()), chatMessageVOS);
        return chatMessageVOS;
    }
    /**
     * 返回消息
     *
     * @param loginUser              登录用户
     * @param userId                 用户id
     * @param chatLambdaQueryWrapper 聊天lambda查询包装器
     * @return {@link List}<{@link ChatMessageVO}>
     */
    private List<ChatMessageVO> returnMessage(User loginUser,
                                              Long userId,
                                              LambdaQueryWrapper<Chat> chatLambdaQueryWrapper) {
        List<Chat> chatList = this.list(chatLambdaQueryWrapper);
        return chatList.stream().map(chat -> {
            ChatMessageVO chatMessageVo = chatResult(chat.getCreateBy(), chat.getText());
            boolean isCaptain = userId != null && userId.equals(chat.getCreateBy());
            if (userService.getById(chat.getCreateBy()).getUserRole() == UserConstant.ADMIN_ROLE || isCaptain) {
                chatMessageVo.setIsAdmin(true);
            }
            if (chat.getCreateBy().equals(loginUser.getId())) {
                chatMessageVo.setIsMy(true);
            }
            chatMessageVo.setCreateTime(DateUtil.format(chat.getCreateTime(), "yyyy年MM月dd日 HH:mm:ss"));
            return chatMessageVo;
        }).collect(Collectors.toList());
    }

    /**
     * 检查是我信息
     *
     * @param loginUser   登录用户
     * @param chatRecords 聊天记录
     * @return {@link List }<{@link ChatMessageVO }>
     */
    private List<ChatMessageVO> checkIsMyMessage(User loginUser, List<ChatMessageVO> chatRecords) {
        return chatRecords.stream().peek(chat -> {
            if (!Objects.equals(chat.getFromUser().getId(), loginUser.getId()) && chat.getIsMy()) {
                chat.setIsMy(false);
            }
            if (Objects.equals(chat.getFromUser().getId(), loginUser.getId()) && !chat.getIsMy()) {
                chat.setIsMy(true);
            }
        }).collect(Collectors.toList());
    }

    /**
     * 获取未读消息数量
     *
     * @param loginId  登录id
     * @param remoteId 遥远id
     * @return {@link Integer}
     */
    private Integer getPrivateChatUnreadNum(Long loginId, Long remoteId, Integer type) {
        LambdaQueryWrapper<Message> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatLambdaQueryWrapper.eq(Message::getCreateBy, remoteId)
                .eq(Message::getToId, loginId)
                .eq(Message::getType, type)
                .eq(Message::getIsRead, MessageReadStatusEnums.UNREAD.getValue());
        return Math.toIntExact(messageService.count(chatLambdaQueryWrapper));
    }

    /**
     * 获取私聊最后一条消息信息
     *
     * @param loginId  登录id
     * @param remoteId 遥远id
     * @return {@link String}
     */
    private Pair<String, Date> getPrivateLastMessage(Long loginId, Long remoteId) {
        LambdaQueryWrapper<Chat> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatLambdaQueryWrapper
                .eq(Chat::getCreateBy, loginId)
                .eq(Chat::getToId, remoteId)
                .eq(Chat::getChatType, 3)
                .orderBy(true, false, Chat::getCreateTime);
        List<Chat> chatList1 = this.list(chatLambdaQueryWrapper);
        chatLambdaQueryWrapper.clear();
        chatLambdaQueryWrapper.eq(Chat::getCreateBy, remoteId)
                .eq(Chat::getToId, loginId)
                .eq(Chat::getChatType, 3)
                .orderBy(true, false, Chat::getCreateTime);
        List<Chat> chatList2 = this.list(chatLambdaQueryWrapper);
        if (chatList1.isEmpty() && chatList2.isEmpty()) {
            return new Pair<>("", null);
        }
        if (chatList1.isEmpty()) {
            return new Pair<>(chatList2.get(0).getText(), chatList2.get(0).getCreateTime());
        }
        if (chatList2.isEmpty()) {
            return new Pair<>(chatList1.get(0).getText(), chatList1.get(0).getCreateTime());
        }
        if (chatList1.get(0).getCreateTime().after(chatList2.get(0).getCreateTime())) {
            return new Pair<>(chatList1.get(0).getText(), chatList1.get(0).getCreateTime());
        } else {
            return new Pair<>(chatList2.get(0).getText(), chatList2.get(0).getCreateTime());
        }
    }

    /**
     * 聊天结果
     *
     * @param userId 用户id
     * @param text   文本
     * @return {@link ChatMessageVO}
     */
    private ChatMessageVO chatResult(Long userId, String text) {
        ChatMessageVO chatMessageVo = new ChatMessageVO();
        User fromUser = userService.getById(userId);
        WebSocketVO fromWebSocketVo = new WebSocketVO();
        BeanUtils.copyProperties(fromUser, fromWebSocketVo);
        chatMessageVo.setFromUser(fromWebSocketVo);
        chatMessageVo.setText(text);
        return chatMessageVo;
    }

    /**
     * 保存缓存
     *
     * @param redisKey       redis键
     * @param id             id
     * @param chatMessageVOS 聊天消息vos
     */
    @Override
    public void saveCache(String redisKey, String id, List<ChatMessageVO> chatMessageVOS) {
        try {
            String messageJSONStr = JsonUtils.objToJson(chatMessageVOS);
            // 解决缓存雪崩
            int i = RandomUtil.randomInt(2, 3);
            if (redisKey.equals(RedisKeyConstant.CACHE_CHAT_HALL)) {
                redisUtils.set(
                        redisKey,
                        messageJSONStr,
                        2 + i / 10,
                        TimeUnit.MINUTES);
            } else {
                redisUtils.set(
                        redisKey + id,
                        messageJSONStr,
                        2 + i / 10,
                        TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            log.error("redis set key error");
        }
    }

    /**
     * 获取缓存
     *
     * @param redisKey redis键
     * @param id       id
     * @return {@link List}<{@link ChatMessageVO}>
     */
    @Override
    public List<ChatMessageVO> getCache(String redisKey, String id) {
        List<ChatMessageVO> chatRecords;
        String messageJSONStr;
        // 官方大厅聊天
        if (redisKey.equals(RedisKeyConstant.CACHE_CHAT_HALL)) {
            messageJSONStr = (String) redisUtils.get(redisKey);
        } else {
            // 私人聊天、队伍聊天等等
            messageJSONStr = (String) redisUtils.get(redisKey + id);
        }
        if (messageJSONStr == null) {
            return null;
        }
        chatRecords = JsonUtils.jsonToList(messageJSONStr, ChatMessageVO.class);
        return chatRecords;
    }


}
