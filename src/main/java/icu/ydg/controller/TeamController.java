package icu.ydg.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import icu.ydg.annotation.Verify;
import icu.ydg.common.ApiResponse;
import icu.ydg.common.ApiResult;
import icu.ydg.common.ErrorCode;
import icu.ydg.constant.UserConstant;
import icu.ydg.model.domain.Team;
import icu.ydg.model.domain.User;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.team.TeamAddRequest;
import icu.ydg.model.dto.team.TeamQueryRequest;
import icu.ydg.model.dto.team.TeamUpdateRequest;
import icu.ydg.model.vo.team.TeamVO;
import icu.ydg.service.TeamService;
import icu.ydg.service.UserService;
import icu.ydg.service.UserTeamService;
import icu.ydg.utils.ThrowUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 队伍接口
 *
 * @author 袁德光
 * @Date 2024/11/03
 */
@RestController
@RequestMapping("/team")
@Slf4j
//@Api(tags = "队伍模块接口")
public class TeamController {

    @Resource
    private TeamService teamService;

    @Resource
    private UserService userService;
    @Resource
    private UserTeamService userTeamService;

    // region 增删改查

    /**
     * 创建队伍
     *
     * @param teamAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "创建队伍")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(teamAddRequest == null, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(teamService.addTeam(teamAddRequest, request));
    }

    /**
     * 删除队伍
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @ApiOperation(value = "删除队伍")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> deleteTeam(@RequestBody IdRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(teamService.deleteTeam(deleteRequest, request), "删除成功！");
    }

    /**
     * 更新队伍（仅管理员可用）
     *
     * @param teamUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @ApiOperation(value = "更新队伍（需要 user 权限）")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(teamUpdateRequest == null || teamUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(teamService.updateTeam(teamUpdateRequest, request), "更新成功！");
    }

    /**
     * 根据 id 获取队伍（封装类）
     *
     * @param idRequest
     * @return
     */
    @GetMapping("/get/vo")
    @ApiOperation(value = "根据 id 获取队伍（封装类）")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<TeamVO> getTeamVOById(IdRequest idRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(idRequest == null || idRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 获取封装类
        return ApiResult.success(teamService.getTeamVO(idRequest, request));
    }

    /**
     * 分页获取队伍列表（仅管理员可用）
     *
     * @param teamQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @ApiOperation(value = "分页获取队伍列表（仅管理员可用）")
    @Verify(checkAuth = UserConstant.ADMIN_ROLE)
    public ApiResponse<Page<Team>> listTeamByPage(@RequestBody TeamQueryRequest teamQueryRequest) {
        ThrowUtils.throwIf(teamQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(teamService.getListPage(teamQueryRequest));
    }

    /**
     * 分页获取队伍列表（封装类）
     *
     * @param teamQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    @ApiOperation(value = "分页获取队伍列表（封装类）")
    public ApiResponse<Page<TeamVO>> listTeamVOByPage(@RequestBody TeamQueryRequest teamQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(teamQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(teamService.handlePaginationAndValidation(teamQueryRequest, request));
    }

    /**
     * 分页获取当前登录用户创建的队伍列表
     *
     * @param teamQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    @ApiOperation(value = "分页获取当前登录用户创建的队伍列表")
    public ApiResponse<Page<TeamVO>> listMyTeamVOByPage(@RequestBody TeamQueryRequest teamQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(teamQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        teamQueryRequest.setCreateBy(loginUser.getId());
        return ApiResult.success(teamService.handlePaginationAndValidation(teamQueryRequest, request));
    }

    // endregion

    /**
     * 分页获取当前登录用户加入的队伍列表
     *
     * @param teamQueryRequest 请求参数（分页条件等）
     * @param request          HttpServletRequest，用于获取当前登录用户
     * @return ApiResponse<Page<TeamVO>> 分页后的队伍列表
     */
    @PostMapping("/joined/list/page/vo")
    @ApiOperation(value = "分页获取当前登录用户加入的队伍列表")
    // @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Page<Team>> listJoinedTeamVOByPage(@RequestBody TeamQueryRequest teamQueryRequest,
            HttpServletRequest request) {
        // 校验请求参数
        ThrowUtils.throwIf(teamQueryRequest == null, ErrorCode.PARAMS_ERROR);

        // 获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();

        // 获取用户加入的队伍 ID 列表
        List<Long> joinedTeamIds = userTeamService.getJoinedTeamIdsByUserId(userId);

        // 如果用户没有加入任何队伍，则返回空分页结果
        if (joinedTeamIds == null || joinedTeamIds.isEmpty()) {
            return ApiResult.success(new Page<>());
        }

        // 设置队伍 ID 列表到查询条件中
        teamQueryRequest.setTeamIds(joinedTeamIds);

        // 调用分页查询服务
        // Page<Team> pageResult = teamService.getTeamVOByIdsWithPagination(teamQueryRequest);

        // 返回分页结果
        return ApiResult.success(teamService.getListPage(teamQueryRequest));
        //  ApiResult.success(pageResult);
    }

}
