package icu.ydg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import icu.ydg.model.domain.Friend;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.friend.FriendAddRequest;
import icu.ydg.model.dto.friend.FriendQueryRequest;
import icu.ydg.model.dto.friend.FriendUpdateRequest;
import icu.ydg.model.vo.friend.FriendVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 好友服务
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
public interface FriendService extends IService<Friend> {

    /**
     * 校验数据
     *
     * @param friend
     * @param add 对创建的数据进行校验
     */
    void validFriend(Friend friend, boolean add);

    /**
     * 获取查询条件
     *
     * @param friendQueryRequest
     * @return
     */
    LambdaQueryWrapper<Friend> getQueryWrapper(FriendQueryRequest friendQueryRequest);
    
    /**
     * 获取好友封装
     *
     * @param idRequest
     * @param request
     * @return
     */
    FriendVO getFriendVO(IdRequest idRequest, HttpServletRequest request);

    /**
     * 分页获取好友封装
     *
     * @param friendPage
     * @param request
     * @return
     */
    Page<FriendVO> getFriendVOPage(Page<Friend> friendPage, HttpServletRequest request);

    /**
    * 更新好友
    *
    * @param friendUpdateRequest 更新后请求
    * @param request           请求
    * @return long
    */
    long updateFriend(FriendUpdateRequest friendUpdateRequest, HttpServletRequest request);

    /**
    * 添加好友
    *
    * @param friendAddRequest 好友添加请求
    * @param request               请求
    * @return long
    */
    long addFriend(FriendAddRequest friendAddRequest, HttpServletRequest request);

    /**
    * 删除好友
    *
    * @param deleteRequest 删除请求
    * @param request       请求
    * @return int
    */
    long deleteFriend(IdRequest deleteRequest, HttpServletRequest request);

    /**
    * “获取列表” 页
    *
    * @param friendQueryRequest
    * @return {@link Page }<{@link Friend }>
    */
    Page<Friend> getListPage(FriendQueryRequest friendQueryRequest);

    /**
    * 处理分页和验证
    *
    * @param friendQueryRequest
    * @param request                 请求
    * @return {@link Page }<{@link FriendVO }>
    */
    Page<FriendVO> handlePaginationAndValidation(FriendQueryRequest friendQueryRequest, HttpServletRequest request);

    /**
    * 只有我或管理员可以做
    *
    * @param request 请求
    * @param id      id
    */
    void onlyMeOrAdministratorCanDo(HttpServletRequest request, Long id);
}
