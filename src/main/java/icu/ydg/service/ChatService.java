package icu.ydg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import icu.ydg.model.domain.Chat;
import icu.ydg.model.domain.User;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.chat.*;
import icu.ydg.model.vo.chat.ChatMessageVO;
import icu.ydg.model.vo.chat.ChatVO;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * 聊天服务
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
public interface ChatService extends IService<Chat> {

    /**
     * 校验数据
     *
     * @param chat
     * @param add 对创建的数据进行校验
     */
    void validChat(Chat chat, boolean add);

    /**
     * 获取查询条件
     *
     * @param chatQueryRequest
     * @return
     */
    LambdaQueryWrapper<Chat> getQueryWrapper(ChatQueryRequest chatQueryRequest);
    
    /**
     * 获取聊天封装
     *
     * @param idRequest
     * @param request
     * @return
     */
    ChatVO getChatVO(IdRequest idRequest, HttpServletRequest request);

    /**
     * 分页获取聊天封装
     *
     * @param chatPage
     * @param request
     * @return
     */
    Page<ChatVO> getChatVOPage(Page<Chat> chatPage, HttpServletRequest request);

    /**
    * 更新聊天
    *
    * @param chatUpdateRequest 更新后请求
    * @param request           请求
    * @return long
    */
    long updateChat(ChatUpdateRequest chatUpdateRequest, HttpServletRequest request);

    /**
    * 添加聊天
    *
    * @param chatAddRequest 聊天添加请求
    * @param request               请求
    * @return long
    */
    long addChat(ChatAddRequest chatAddRequest, HttpServletRequest request);

    /**
    * 删除聊天
    *
    * @param deleteRequest 删除请求
    * @param request       请求
    * @return int
    */
    long deleteChat(IdRequest deleteRequest, HttpServletRequest request);

    /**
    * “获取列表” 页
    *
    * @param chatQueryRequest
    * @return {@link Page }<{@link Chat }>
    */
    Page<Chat> getListPage(ChatQueryRequest chatQueryRequest);

    /**
    * 处理分页和验证
    *
    * @param chatQueryRequest
    * @param request                 请求
    * @return {@link Page }<{@link ChatVO }>
    */
    Page<ChatVO> handlePaginationAndValidation(ChatQueryRequest chatQueryRequest, HttpServletRequest request);

    /**
    * 只有我或管理员可以做
    *
    * @param request 请求
    * @param id      id
    */
    void onlyMeOrAdministratorCanDo(HttpServletRequest request, Long id);

    /**
     * 获取聊天
     *
     * @param chatRequest 聊天请求
     * @param chatType    聊天类型
     * @param loginUser   登录用户
     * @return {@link List }<{@link ChatMessageVO }>
     */
    List<ChatMessageVO> getChat(ChatRequest chatRequest, int chatType, User loginUser);

    /**
     * 获取缓存
     *
     * @param redisKey redis密钥
     * @param id       id
     * @return {@link List }<{@link ChatMessageVO }>
     */
    List<ChatMessageVO> getCache(String redisKey, String id);

    /**
     * 保存缓存
     *
     * @param redisKey       redis密钥
     * @param id             id
     * @param chatMessageVOS 聊天消息vos
     */
    void saveCache(String redisKey, String id, List<ChatMessageVO> chatMessageVOS);

    /**
     * 聊天结果
     *
     * @param userId     用户id
     * @param toId       至id
     * @param text       文本
     * @param chatType   聊天类型
     * @param createTime 创建时间
     * @return {@link ChatMessageVO }
     */
    ChatMessageVO chatResult(Long userId, Long toId, String text, Integer chatType, Date createTime);

    /**
     * 获取私人列表
     *
     * @param id id
     * @return {@link List }<{@link PrivateChatVO }>
     */
    List<PrivateChatVO> getPrivateList(Long id);

    /**
     * 阅读私人信息
     *
     * @param id       id
     * @param remoteId 远程id
     * @return {@link Boolean }
     */
    Boolean readPrivateMessage(Long id, Long remoteId);

    /**
     * 获取团队聊天
     *
     * @param chatRequest 聊天请求
     * @param teamChat    团队聊天
     * @param loginUser   登录用户
     * @return {@link List}<{@link ChatMessageVO}>
     */
    List<ChatMessageVO> getTeamChat(ChatRequest chatRequest, int teamChat, User loginUser);

    /**
     * 获得大厅聊天
     *
     * @param chatType  聊天类型
     * @param loginUser 登录用户
     * @return {@link List}<{@link ChatMessageVO}>
     */
    List<ChatMessageVO> getHallChat(int chatType, User loginUser);


    /**
     * get un read private num
     *
     * @param userId 用户id
     * @return {@link Integer }
     */
    Integer getUnReadPrivateNum(Long userId);

    /**
     * 删除密钥
     *
     * @param key 钥匙
     * @param id  id
     */
    void deleteKey(String key, String id);

    /**
     * 获取团队列表
     *
     * @param id id
     * @return {@link List }<{@link PrivateChatVO }>
     */
    List<PrivateChatVO> getTeamList(Long id);

    /**
     * 团队num
     *
     * @param id id
     * @return {@link List }<{@link PrivateChatVO }>
     */
    Integer getUnReadTeamNum(Long id);
}
