package icu.ydg.ws;


import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import icu.ydg.config.HttpSessionConfig;
import icu.ydg.constant.RedisKeyConstant;
import icu.ydg.constant.UserConstant;
import icu.ydg.model.domain.*;
import icu.ydg.model.dto.message.MessageRequest;
import icu.ydg.model.enums.message.MessageTypeEnums;
import icu.ydg.model.vo.chat.ChatMessageVO;
import icu.ydg.model.vo.ws.WebSocketVO;
import icu.ydg.service.*;
import icu.ydg.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * web套接字
 *
 * @author 袁德光
 * @date 2024/11/30
 */
@Component
@Slf4j
@ServerEndpoint(value = "/websocket/{userId}/{teamId}", configurator = HttpSessionConfig.class)
public class WebSocket {
    /**
     * 保存队伍的连接信息
     */
    private static final Map<String, ConcurrentHashMap<String, WebSocket>> ROOMS = new HashMap<>();

    /**
     * 线程安全的无序的集合
     */
    private static final CopyOnWriteArraySet<Session> SESSIONS = new CopyOnWriteArraySet<>();

    /**
     * 会话池
     */
    private static final Map<String, Session> SESSION_POOL = new HashMap<>(0);
    /**
     * 用户服务
     */
    private static UserService userService;
    /**
     * 聊天服务
     */
    private static ChatService chatService;
    /**
     * 聊天服务
     */
    private static MessageService messageService;
    /**
     * 团队服务
     */
    private static TeamService teamService;
    /**
     * 团队服务
     */
    private static UserTeamService userTeamService;
    /**
     * 房间在线人数
     */
    private static int onlineCount = 0;

    /**
     * 当前信息
     */
    private Session session;

    /**
     * http会话
     */
    private HttpSession httpSession;

    /**
     * 上网数
     *
     * @return int
     */
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    /**
     * 添加在线计数
     */
    public static synchronized void addOnlineCount() {
        WebSocket.onlineCount++;
    }

    /**
     * 子在线计数
     */
    public static synchronized void subOnlineCount() {
        WebSocket.onlineCount--;
    }

    /**
     * 集热地图服务
     *
     * @param userService 用户服务
     */
    @Resource
    public void setHeatMapService(UserService userService) {
        WebSocket.userService = userService;
    }

    /**
     * 集热地图服务
     *
     * @param chatService 聊天服务
     */
    @Resource
    public void setHeatMapService(ChatService chatService) {
        WebSocket.chatService = chatService;
    }

    /**
     * 设置热图服务
     *
     * @param messageService 消息服务
     */
    @Resource
    public void setHeatMapService(MessageService messageService) {
        WebSocket.messageService = messageService;
    }

    /**
     * 集热地图服务
     *
     * @param teamService 团队服务
     */
    @Resource
    public void setHeatMapService(TeamService teamService) {
        WebSocket.teamService = teamService;
    }
    /**
     * 集热地图服务
     *
     * @param userTeamService 团队服务
     */
    @Resource
    public void setHeatMapService(UserTeamService userTeamService) {
        WebSocket.userTeamService = userTeamService;
    }
    /**
     * 队伍内群发消息
     *
     * @param teamId 团队id
     * @param msg    消息
     */
    public static void broadcast(String teamId, String msg) {
        ConcurrentHashMap<String, WebSocket> map = ROOMS.get(teamId);
        // keySet获取map集合key的集合  然后在遍历key即可
        for (String key : map.keySet()) {
            try {
                WebSocket webSocket = map.get(key);
                webSocket.sendMessage(msg);
            } catch (Exception e) {
                log.error("exception message", e);
            }
        }
    }

    /**
     * 发送消息
     *
     * @param message 消息
     * @throws IOException ioexception
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    /**
     * 开放
     *
     * @param session 会话
     * @param userId  用户id
     * @param teamId  团队id
     * @param config  配置
     */
    @OnOpen
    public void onOpen(Session session,
                       @PathParam(value = "userId") String userId,
                       @PathParam(value = "teamId") String teamId,
                       EndpointConfig config) {
        try {
            if (StringUtils.isBlank(userId) || "undefined".equals(userId)) {
                sendError(userId, "参数有误");
                return;
            }
            HttpSession userHttpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
            User user = (User) userHttpSession.getAttribute(UserConstant.USER_LOGIN_STATE);
            if (user != null) {
                this.session = session;
                this.httpSession = userHttpSession;
            }
            if (!"NaN".equals(teamId)) {
                if (!ROOMS.containsKey(teamId)) {
                    ConcurrentHashMap<String, WebSocket> room = new ConcurrentHashMap<>(0);
                    room.put(userId, this);
                    ROOMS.put(String.valueOf(teamId), room);
                    // 在线数加1
                    addOnlineCount();
                } else {
                    if (!ROOMS.get(teamId).containsKey(userId)) {
                        ROOMS.get(teamId).put(userId, this);
                        // 在线数加1
                        addOnlineCount();
                    }
                }
            } else {
                SESSIONS.add(session);
                SESSION_POOL.put(userId, session);
                sendAllUsers();
            }
        } catch (Exception e) {
            log.error("exception message", e);
        }
    }

    /**
     * 关闭
     *
     * @param userId  用户id
     * @param teamId  团队id
     * @param session 会话
     */
    @OnClose
    public void onClose(@PathParam("userId") String userId,
                        @PathParam(value = "teamId") String teamId,
                        Session session) {
        try {
            if (!"NaN".equals(teamId)) {
                ROOMS.get(teamId).remove(userId);
                if (getOnlineCount() > 0) {
                    subOnlineCount();
                }
            } else {
                if (!SESSION_POOL.isEmpty()) {
                    SESSION_POOL.remove(userId);
                    SESSIONS.remove(session);
                }
                sendAllUsers();
            }
        } catch (Exception e) {
            log.error("exception message", e);
        }
    }

    /**
     * 消息
     *
     * @param message 消息
     * @param userId  用户id
     */
    @OnMessage
    public void onMessage(String message, @PathParam("userId") String userId) {
        if ("PING".equals(message)) {
            sendOneMessage(userId, "pong");
            return;
        }
        MessageRequest messageRequest = JsonUtils.jsonToObj(message, MessageRequest.class);
        assert messageRequest != null;
        Long toId = messageRequest.getToId();
        Long teamId = messageRequest.getTeamId();
        String text = messageRequest.getText();
        Integer chatType = messageRequest.getChatType();
        Integer textType = messageRequest.getTextType();
        User fromUser = userService.getById(userId);
        Team team = teamService.getById(teamId);
        if (chatType == 3) {
            // 私聊
            privateChat(fromUser, toId, text, chatType,textType);
        } else if (chatType == 4) {
            // 队伍内聊天
            teamChat(fromUser, text, team, chatType,textType);
        } else {
            // 群聊
            hallChat(fromUser, text, chatType,textType);
        }
    }

    /**
     * 队伍聊天
     *
     * @param user     用户
     * @param text     文本
     * @param team     团队
     * @param chatType 聊天类型
     */
    private void teamChat(User user, String text, Team team, Integer chatType, Integer textType) {
        ChatMessageVO chatMessageVo = new ChatMessageVO();
        WebSocketVO fromWebSocketVO = new WebSocketVO();
        BeanUtils.copyProperties(user, fromWebSocketVO);
        chatMessageVo.setFromUser(fromWebSocketVO);
        chatMessageVo.setText(text);
        chatMessageVo.setTeamId(team.getId());
        chatMessageVo.setChatType(chatType);
        chatMessageVo.setTextType(textType);
        chatMessageVo.setCreateTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        if (Objects.equals(user.getId(), team.getCreateBy()) || Objects.equals(user.getUserRole(), UserConstant.ADMIN_ROLE)) {
            chatMessageVo.setIsAdmin(true);
        }
        User loginUser = (User) this.httpSession.getAttribute(UserConstant.USER_LOGIN_STATE);
        if (Objects.equals(loginUser.getId(), user.getId())) {
            chatMessageVo.setIsMy(true);
        }
        String toJson = JsonUtils.objToJson(chatMessageVo);
        try {
            broadcast(String.valueOf(team.getId()), toJson);
            saveChat(user.getId(), null, text, team.getId(), chatType,textType);
            saveMessage(user.getId(), null, text, team.getId(), chatType,textType);
            chatService.deleteKey(RedisKeyConstant.CACHE_CHAT_TEAM, String.valueOf(team.getId()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 大厅聊天
     *
     * @param user     用户
     * @param text     文本
     * @param chatType 聊天类型
     */
    private void hallChat(User user, String text, Integer chatType,Integer textType) {
        ChatMessageVO chatMessageVo = new ChatMessageVO();
        WebSocketVO fromWebSocketVO = new WebSocketVO();
        BeanUtils.copyProperties(user, fromWebSocketVO);
        chatMessageVo.setFromUser(fromWebSocketVO);
        chatMessageVo.setText(text);
        chatMessageVo.setChatType(chatType);
        chatMessageVo.setTextType(textType);
        chatMessageVo.setCreateTime(DateUtil.format(new Date(), "yyyy年MM月dd日 HH:mm:ss"));
        if (Objects.equals(user.getUserRole(), UserConstant.ADMIN_ROLE)) {
            chatMessageVo.setIsAdmin(true);
        }
        User loginUser = (User) this.httpSession.getAttribute(UserConstant.USER_LOGIN_STATE);
        if (Objects.equals(loginUser.getId(), user.getId())) {
            chatMessageVo.setIsMy(true);
        }
        String toJson = JsonUtils.objToJson(chatMessageVo);
        sendAllMessage(toJson);
        saveChat(user.getId(), null, text, null, chatType,textType);
        saveMessage(user.getId(), null, text, null, chatType,textType);
        chatService.deleteKey(RedisKeyConstant.CACHE_CHAT_HALL, String.valueOf(user.getId()));
    }

    /**
     * 私聊
     *
     * @param user     用户
     * @param toId     为id
     * @param text     文本
     * @param chatType 聊天类型
     */
    private void privateChat(User user, Long toId, String text, Integer chatType, Integer textType) {
        ChatMessageVO chatMessageVo = chatService
                .chatResult(user.getId(), toId, text, chatType, DateUtil.date(System.currentTimeMillis()));
        User loginUser = (User) this.httpSession.getAttribute(UserConstant.USER_LOGIN_STATE);
        if (Objects.equals(loginUser.getId(), user.getId())) {
            chatMessageVo.setIsMy(true);
        }
        String toJson = JsonUtils.objToJson(chatMessageVo);
        sendOneMessage(toId.toString(), toJson);
        saveChat(user.getId(), toId, text, null, chatType,textType);
        saveMessage(user.getId(), toId, text, null, chatType,textType);
        chatService.deleteKey(RedisKeyConstant.CACHE_CHAT_PRIVATE, user.getId() + "-" + toId);
        chatService.deleteKey(RedisKeyConstant.CACHE_CHAT_PRIVATE, toId + "-" + user.getId());
    }

    /**
     * 保存聊天
     *
     * @param userId   用户id
     * @param toId     为id
     * @param text     文本
     * @param teamId   团队id
     * @param chatType 聊天类型
     */
    private void saveChat(Long userId, Long toId, String text, Long teamId, Integer chatType,Integer textType) {
//        if (chatType == PRIVATE_CHAT) {
//            User user = userService.getById(userId);
//            Set<Long> userIds = stringJsonListToLongSet(user.getFriendIds());
//            if (!userIds.contains(toId)) {
//                sendError(String.valueOf(userId), "该用户不是你的好友");
//                return;
//            }
//        }
        Chat chat = new Chat();
        chat.setCreateBy(userId);
        chat.setText(String.valueOf(text));
        chat.setChatType(chatType);
        chat.setTextType(textType);
        chat.setCreateTime(new Date());
        chat.setUpdateBy(userId);
        if (toId != null && toId > 0) {
            chat.setToId(toId);
        }
        if (teamId != null && teamId > 0) {
            chat.setTeamId(teamId);
        }
        chatService.save(chat);
    }

    /**
     * 保存消息
     *
     * @param userId   用户id
     * @param toId     至id
     * @param text     文本
     * @param teamId   团队id
     * @param chatType 聊天类型
     */
    private void saveMessage(Long userId, Long toId, String text, Long teamId, Integer chatType,Integer textType) {
        // 队伍，添加到每个队员消息
        if (chatType.equals(MessageTypeEnums.TEAM_CHAT.getValue()) && teamId != null && teamId > 0) {
            //    获取到每个队员的id
            List<Long> memberIds = userTeamService.getMemberIds(teamId);
            // 为每个队员储存消息
            for (Long memberId : memberIds) {
                Message message = new Message();
                message.setType(chatType);
                message.setContent(String.valueOf(text));
                message.setContentType(textType);
                message.setIsRead(0);
                message.setCreateBy(userId);
                message.setUpdateBy(userId);
                message.setToId(memberId);
                message.setTeamId(teamId);
                messageService.save(message);
            }
        }
        // 私聊
        if (chatType.equals(MessageTypeEnums.PRIVATE_CHAT.getValue()) && toId != null && toId > 0) {
            Message message = new Message();
            message.setType(chatType);
            message.setContent(String.valueOf(text));
            message.setContentType(textType);
            message.setIsRead(0);
            message.setCreateBy(userId);
            message.setUpdateBy(userId);
            message.setToId(toId);
            messageService.save(message);
        }
        // 官方聊天，为所有用户储存未读的消息
        if (chatType.equals(MessageTypeEnums.OFFICIAL_CHAT.getValue())) {
            // 查询所有用户
            List<User> userList = userService.list();
            for (User user : userList) {
                Message message = new Message();
                message.setType(chatType);
                message.setContent(String.valueOf(text));
                message.setContentType(textType);
                message.setIsRead(0);
                message.setCreateBy(userId);
                message.setUpdateBy(userId);
                message.setToId(user.getId());
                messageService.save(message);
            }
        }
    }

    /**
     * 发送失败
     *
     * @param userId       用户id
     * @param errorMessage 错误消息
     */
    private void sendError(String userId, String errorMessage) {
        JSONObject obj = new JSONObject();
        obj.set("error", errorMessage);
        sendOneMessage(userId, obj.toString());
    }

    /**
     * 广播消息
     *
     * @param message 消息
     */
    public void sendAllMessage(String message) {
        for (Session userSession : SESSIONS) {
            try {
                if (userSession.isOpen()) {
                    synchronized (userSession) {
                        userSession.getBasicRemote().sendText(message);
                    }
                }
            } catch (Exception e) {
                log.error("exception message", e);
            }
        }
    }


    /**
     * 发送一个消息
     *
     * @param userId  用户编号
     * @param message 消息
     */
    public void sendOneMessage(String userId, String message) {
        Session userSession = SESSION_POOL.get(userId);
        if (userSession != null && userSession.isOpen()) {
            try {
                synchronized (userSession) {
                    userSession.getBasicRemote().sendText(message);
                }
            } catch (Exception e) {
                log.error("exception message", e);
            }
        }
    }

    /**
     * 给所有用户
     */
    public void sendAllUsers() {
        HashMap<String, List<WebSocketVO>> stringListHashMap = new HashMap<>(0);
        List<WebSocketVO> webSocketVos = new ArrayList<>();
        stringListHashMap.put("users", webSocketVos);
        for (Serializable key : SESSION_POOL.keySet()) {
            User user = userService.getById(key);
            WebSocketVO webSocketVO = new WebSocketVO();
            BeanUtils.copyProperties(user, webSocketVO);
            webSocketVos.add(webSocketVO);
        }
        sendAllMessage(JSONUtil.toJsonStr(stringListHashMap));
    }
}
