package icu.ydg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import icu.ydg.model.domain.UserTeam;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.userTeam.UserKickRequest;
import icu.ydg.model.dto.userTeam.UserTeamAddRequest;
import icu.ydg.model.dto.userTeam.UserTeamQueryRequest;
import icu.ydg.model.dto.userTeam.UserTeamUpdateRequest;
import icu.ydg.model.vo.userTeam.UserTeamVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户队伍服务
 *
 * @author 袁德光
 * @Date 2024/11/03
 */
public interface UserTeamService extends IService<UserTeam> {

    /**
     * 校验数据
     *
     * @param userTeam
     * @param add 对创建的数据进行校验
     */
    void validUserTeam(UserTeam userTeam, boolean add);

    /**
     * 获取查询条件
     *
     * @param userTeamQueryRequest
     * @return
     */
    LambdaQueryWrapper<UserTeam> getQueryWrapper(UserTeamQueryRequest userTeamQueryRequest);
    
    /**
     * 获取用户队伍封装
     *
     * @param idRequest
     * @param request
     * @return
     */
    UserTeamVO getUserTeamVO(IdRequest idRequest, HttpServletRequest request);

    /**
     * 分页获取用户队伍封装
     *
     * @param userTeamPage
     * @param request
     * @return
     */
    Page<UserTeamVO> getUserTeamVOPage(Page<UserTeam> userTeamPage, HttpServletRequest request);

    /**
    * 更新用户队伍
    *
    * @param userTeamUpdateRequest 更新后请求
    * @param request           请求
    * @return long
    */
    long updateUserTeam(UserTeamUpdateRequest userTeamUpdateRequest, HttpServletRequest request);

    /**
    * 添加用户队伍
    *
    * @param userTeamAddRequest 用户队伍添加请求
    * @param request               请求
    * @return long
    */
    long addUserTeam(UserTeamAddRequest userTeamAddRequest, HttpServletRequest request);

    /**
    * 删除用户队伍
    *
    * @param deleteRequest 删除请求
    * @param request       请求
    * @return int
    */
    long deleteUserTeam(IdRequest deleteRequest, HttpServletRequest request);

    /**
    * “获取列表” 页
    *
    * @param userTeamQueryRequest
    * @return {@link Page }<{@link UserTeam }>
    */
    Page<UserTeam> getListPage(UserTeamQueryRequest userTeamQueryRequest);

    /**
    * 处理分页和验证
    *
    * @param userTeamQueryRequest
    * @param request                 请求
    * @return {@link Page }<{@link UserTeamVO }>
    */
    Page<UserTeamVO> handlePaginationAndValidation(UserTeamQueryRequest userTeamQueryRequest, HttpServletRequest request);

    /**
    * 只有我或管理员可以做
    *
    * @param request 请求
    * @param id      id
    */
    void onlyMeOrAdministratorCanDo(HttpServletRequest request, Long id);

    /**
     * 按用户id获取已加入团队id
     *
     * @param userId 用户id
     * @return {@link List }<{@link Long }>
     */
    List<Long> getJoinedTeamIdsByUserId(Long userId);

    /**
     * kick用户
     *
     * @param userKickRequest 用户踢请求
     * @param request         请求
     * @return int
     */
    int kickUser(UserKickRequest userKickRequest, HttpServletRequest request);
}
