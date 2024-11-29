package icu.ydg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import icu.ydg.model.domain.Follow;
import icu.ydg.model.domain.User;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.follow.FollowAddRequest;
import icu.ydg.model.dto.follow.FollowQueryRequest;
import icu.ydg.model.dto.follow.FollowUpdateRequest;
import icu.ydg.model.vo.follow.FollowVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 关注服务
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
public interface FollowService extends IService<Follow> {

    /**
     * 校验数据
     *
     * @param follow
     * @param add 对创建的数据进行校验
     */
    void validFollow(Follow follow, boolean add);

    /**
     * 获取查询条件
     *
     * @param followQueryRequest
     * @return
     */
    LambdaQueryWrapper<Follow> getQueryWrapper(FollowQueryRequest followQueryRequest);
    
    /**
     * 获取关注封装
     *
     * @param idRequest
     * @param request
     * @return
     */
    FollowVO getFollowVO(IdRequest idRequest, HttpServletRequest request);

    /**
     * 分页获取关注封装
     *
     * @param followPage
     * @param request
     * @return
     */
    Page<FollowVO> getFollowVOPage(Page<Follow> followPage, HttpServletRequest request);

    /**
    * 更新关注
    *
    * @param followUpdateRequest 更新后请求
    * @param request           请求
    * @return long
    */
    long updateFollow(FollowUpdateRequest followUpdateRequest, HttpServletRequest request);

    /**
    * 添加关注
    *
    * @param followAddRequest 关注添加请求
    * @param request               请求
    * @return long
    */
    long addFollow(FollowAddRequest followAddRequest, HttpServletRequest request);

    /**
    * 删除关注
    *
    * @param deleteRequest 删除请求
    * @param request       请求
    * @return int
    */
    long deleteFollow(IdRequest deleteRequest, HttpServletRequest request);

    /**
    * “获取列表” 页
    *
    * @param followQueryRequest
    * @return {@link Page }<{@link Follow }>
    */
    Page<Follow> getListPage(FollowQueryRequest followQueryRequest);

    /**
    * 处理分页和验证
    *
    * @param followQueryRequest
    * @param request                 请求
    * @return {@link Page }<{@link FollowVO }>
    */
    Page<FollowVO> handlePaginationAndValidation(FollowQueryRequest followQueryRequest, HttpServletRequest request);

    /**
    * 只有我或管理员可以做
    *
    * @param request 请求
    * @param id      id
    */
    void onlyMeOrAdministratorCanDo(HttpServletRequest request, Long id);

    String followUser(User loginUser, Long id);

    String unfollowUser(User loginUser, Long id);

    boolean checkFollowStatus(User loginUser, Long id);
}
