package icu.ydg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import icu.ydg.model.domain.Team;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.team.TeamAddRequest;
import icu.ydg.model.dto.team.TeamQueryRequest;
import icu.ydg.model.dto.team.TeamUpdateRequest;
import icu.ydg.model.vo.team.TeamVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 队伍服务
 *
 * @author 袁德光
 * @Date 2024/11/03
 */
public interface TeamService extends IService<Team> {

    /**
     * 获取查询条件
     *
     * @param teamQueryRequest
     * @return
     */
    LambdaQueryWrapper<Team> getQueryWrapper(TeamQueryRequest teamQueryRequest);
    
    /**
     * 获取队伍封装
     *
     * @param idRequest
     * @param request
     * @return
     */
    TeamVO getTeamVO(IdRequest idRequest, HttpServletRequest request);

    /**
     * 分页获取队伍封装
     *
     * @param teamPage
     * @param request
     * @return
     */
    Page<TeamVO> getTeamVOPage(Page<Team> teamPage, HttpServletRequest request);

    /**
    * 更新队伍
    *
    * @param teamUpdateRequest 更新后请求
    * @param request           请求
    * @return long
    */
    long updateTeam(TeamUpdateRequest teamUpdateRequest, HttpServletRequest request);

    /**
    * 添加队伍
    *
    * @param teamAddRequest 队伍添加请求
    * @param request               请求
    * @return long
    */
    long addTeam(TeamAddRequest teamAddRequest, HttpServletRequest request);

    /**
    * 删除队伍
    *
    * @param deleteRequest 删除请求
    * @param request       请求
    * @return int
    */
    long deleteTeam(IdRequest deleteRequest, HttpServletRequest request);

    /**
    * “获取列表” 页
    *
    * @param teamQueryRequest
    * @return {@link Page }<{@link Team }>
    */
    Page<Team> getListPage(TeamQueryRequest teamQueryRequest);

    /**
    * 处理分页和验证
    *
    * @param teamQueryRequest
    * @param request                 请求
    * @return {@link Page }<{@link TeamVO }>
    */
    Page<TeamVO> handlePaginationAndValidation(TeamQueryRequest teamQueryRequest, HttpServletRequest request);

    /**
    * 只有我或管理员可以做
    *
    * @param request 请求
    * @param id      id
    */
    void onlyMeOrAdministratorCanDo(HttpServletRequest request, Long id);

    // Page<TeamVO> getTeamVOByIdsWithPagination(TeamQueryRequest teamQueryRequest);
}
