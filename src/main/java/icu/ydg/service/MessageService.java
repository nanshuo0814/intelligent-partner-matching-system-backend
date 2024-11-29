package icu.ydg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import icu.ydg.model.domain.Message;
import icu.ydg.model.domain.Post;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.message.MessageAddRequest;
import icu.ydg.model.dto.message.MessageQueryRequest;
import icu.ydg.model.dto.message.MessageUpdateRequest;
import icu.ydg.model.vo.message.MessageVO;
import icu.ydg.model.vo.post.PostVO;
import icu.ydg.model.vo.user.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 消息服务
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
public interface MessageService extends IService<Message> {

    /**
     * 校验数据
     *
     * @param message
     * @param add 对创建的数据进行校验
     */
    void validMessage(Message message, boolean add);

    /**
     * 获取查询条件
     *
     * @param messageQueryRequest
     * @return
     */
    LambdaQueryWrapper<Message> getQueryWrapper(MessageQueryRequest messageQueryRequest);
    
    /**
     * 获取消息封装
     *
     * @param idRequest
     * @param request
     * @return
     */
    MessageVO getMessageVO(IdRequest idRequest, HttpServletRequest request);

    /**
     * 分页获取消息封装
     *
     * @param messagePage
     * @param request
     * @return
     */
    Page<MessageVO> getMessageVOPage(Page<Message> messagePage, HttpServletRequest request);

    /**
    * 更新消息
    *
    * @param messageUpdateRequest 更新后请求
    * @param request           请求
    * @return long
    */
    long updateMessage(MessageUpdateRequest messageUpdateRequest, HttpServletRequest request);

    /**
    * 添加消息
    *
    * @param messageAddRequest 消息添加请求
    * @param request               请求
    * @return long
    */
    long addMessage(MessageAddRequest messageAddRequest, HttpServletRequest request);

    /**
    * 删除消息
    *
    * @param deleteRequest 删除请求
    * @param request       请求
    * @return int
    */
    long deleteMessage(IdRequest deleteRequest, HttpServletRequest request);

    /**
    * “获取列表” 页
    *
    * @param messageQueryRequest
    * @return {@link Page }<{@link Message }>
    */
    Page<Message> getListPage(MessageQueryRequest messageQueryRequest);

    /**
    * 处理分页和验证
    *
    * @param messageQueryRequest
    * @param request                 请求
    * @return {@link Page }<{@link MessageVO }>
    */
    Page<MessageVO> handlePaginationAndValidation(MessageQueryRequest messageQueryRequest, HttpServletRequest request);

    /**
    * 只有我或管理员可以做
    *
    * @param request 请求
    * @param id      id
    */
    void onlyMeOrAdministratorCanDo(HttpServletRequest request, Long id);

    /**
     * 有新消息
     *
     * @param id id
     * @return boolean
     */
    boolean hasNewMessage(Long id);

    /**
     * 获取消息num
     *
     * @param id id
     * @return long
     */
    long getMessageNum(Long id);

    /**
     * 获得帖子好评num
     *
     * @param id id
     * @return long
     */
    long getPostPraiseNum(Long id);

    /**
     * 获取用户帖子
     *
     * @param id id
     * @return {@link List }<{@link PostVO }>
     */
    List<Post> getUserPost(Long id);

    /**
     * 获取好友num
     *
     * @param id id
     * @return long
     */
    long getFriendNum(Long id);

    /**
     * 获取用户vo
     *
     * @param id id
     * @return {@link List }<{@link UserVO }>
     */
    List<UserVO> getUserVO(Long id);

    /**
     * 获取粉丝num
     *
     * @param id id
     * @return long
     */
    long getFansNum(Long id);

    /**
     * 获得粉丝
     *
     * @param id id
     * @return {@link List }<{@link UserVO }>
     */
    List<UserVO> getFans(Long id);

    long getPostCommentNum(Long id);

    long getFollowNum(Long id);
}
