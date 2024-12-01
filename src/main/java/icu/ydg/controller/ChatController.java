package icu.ydg.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import icu.ydg.annotation.Verify;
import icu.ydg.common.ApiResponse;
import icu.ydg.common.ApiResult;
import icu.ydg.common.ErrorCode;
import icu.ydg.constant.UserConstant;
import icu.ydg.exception.BusinessException;
import icu.ydg.model.domain.Chat;
import icu.ydg.model.domain.User;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.chat.*;
import icu.ydg.model.vo.chat.ChatMessageVO;
import icu.ydg.model.vo.chat.ChatVO;
import icu.ydg.service.ChatService;
import icu.ydg.service.UserService;
import icu.ydg.utils.ThrowUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 聊天接口
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@RestController
@RequestMapping("/chat")
@Slf4j
//@Api(tags = "聊天接口")
public class ChatController {

    @Resource
    private ChatService chatService;

    @Resource
    private UserService userService;

    /**
     * 大厅聊天
     *
     * @param request 请求
     * @return {@link ApiResponse}<{@link List}<{@link ChatMessageVO}>>
     */
    @GetMapping("/hallChat")
    @ApiOperation(value = "获取大厅聊天")
    public ApiResponse<List<ChatMessageVO>> getHallChat(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        List<ChatMessageVO> hallChat = chatService.getHallChat(5, loginUser);
        return ApiResult.success(hallChat);
    }

    /**
     * 团队聊天
     *
     * @param chatRequest 聊天请求
     * @param request     请求
     * @return {@link ApiResponse}<{@link List}<{@link ChatMessageVO}>>
     */
    @PostMapping("/teamChat")
    @ApiOperation(value = "获取队伍聊天")
    public ApiResponse<List<ChatMessageVO>> getTeamChat(@RequestBody ChatRequest chatRequest,
                                                         HttpServletRequest request) {
        if (chatRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求有误");
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        List<ChatMessageVO> teamChat = chatService.getTeamChat(chatRequest, 4, loginUser);
        return ApiResult.success(teamChat);
    }

    /**
     * 阅读私人信息
     *
     * @param request  请求
     * @param remoteId 远程id
     * @return {@link ApiResponse }<{@link Boolean }>
     */
    @PostMapping("/private/read")
    public ApiResponse<Boolean> readPrivateMessage(HttpServletRequest request, Long remoteId) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Boolean flag = chatService.readPrivateMessage(loginUser.getId(), remoteId);
        return ApiResult.success(flag);
    }

    /**
     * 获取私聊未读消息数量
     *
     * @param request 要求
     * @return {@link ApiResponse}<{@link Integer}>
     */
    @GetMapping("/private/num")
    @ApiOperation(value = "获取私聊未读消息数量")
    public ApiResponse<Integer> getUnreadPrivateNum(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Integer unreadNum = chatService.getUnReadPrivateNum(loginUser.getId());
        return ApiResult.success(unreadNum);
    }

    /**
     * 获取私人聊天列表
     *
     * @param request 请求
     * @return {@link ApiResponse }<{@link List }<{@link PrivateChatVO }>>
     */
    @GetMapping("/private")
    @ApiOperation(value = "获取私聊列表")
    public ApiResponse<List<PrivateChatVO>> getPrivateChatList(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        List<PrivateChatVO> userList = chatService.getPrivateList(loginUser.getId());
        return ApiResult.success(userList);
    }

    /**
     * 获取团队聊天列表
     *
     * @param request 请求
     * @return {@link ApiResponse }<{@link List }<{@link PrivateChatVO }>>
     */
    @GetMapping("/team")
    @ApiOperation(value = "获取队伍列表")
    public ApiResponse<List<PrivateChatVO>> getTeamChatList(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        List<PrivateChatVO> teamList = chatService.getTeamList(loginUser.getId());
        return ApiResult.success(teamList);
    }

    /**
     * 获取团队聊天消息条数
     *
     * @param request 请求
     * @return {@link ApiResponse }<{@link List }<{@link PrivateChatVO }>>
     */
    @GetMapping("/team/num")
    @ApiOperation(value = "获取队伍未读消息数量")
    public ApiResponse<Integer> getUnreadTeamNum(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Integer num = chatService.getUnReadTeamNum(loginUser.getId());
        return ApiResult.success(num);
    }

    /**
     * 获取私人聊天
     *
     * @param chatRequest 聊天请求
     * @param request     请求
     * @return {@link ApiResponse }<{@link List }<{@link ChatMessageVO }>>
     */
    @PostMapping("/privateChat")
    @ApiOperation(value = "获取私聊")
    public ApiResponse<List<ChatMessageVO>> getPrivateChat(@RequestBody ChatRequest chatRequest,
                                                           HttpServletRequest request) {
        if (chatRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        List<ChatMessageVO> privateChat = chatService.getChat(chatRequest, 3, loginUser);
        return ApiResult.success(privateChat);
    }

    // region 增删改查

    /**
     * 创建聊天
     *
     * @param chatAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "创建聊天")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> addChat(@RequestBody ChatAddRequest chatAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(chatAddRequest == null, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(chatService.addChat(chatAddRequest, request));
    }

    /**
     * 删除聊天
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @ApiOperation(value = "删除聊天")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> deleteChat(@RequestBody IdRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(chatService.deleteChat(deleteRequest, request), "删除成功！");
    }

    /**
     * 更新聊天（仅管理员可用）
     *
     * @param chatUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @ApiOperation(value = "更新聊天（需要 user 权限）")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> updateChat(@RequestBody ChatUpdateRequest chatUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(chatUpdateRequest == null || chatUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(chatService.updateChat(chatUpdateRequest, request), "更新成功！");
    }

    /**
     * 根据 id 获取聊天（封装类）
     *
     * @param idRequest
     * @return
     */
    @GetMapping("/get/vo")
    @ApiOperation(value = "根据 id 获取聊天（封装类）")
    public ApiResponse<ChatVO> getChatVOById(IdRequest idRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(idRequest == null || idRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 获取封装类
        return ApiResult.success(chatService.getChatVO(idRequest, request));
    }

    /**
     * 分页获取聊天列表（仅管理员可用）
     *
     * @param chatQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @ApiOperation(value = "分页获取聊天列表（仅管理员可用）")
    @Verify(checkAuth = UserConstant.ADMIN_ROLE)
    public ApiResponse<Page<Chat>> listChatByPage(@RequestBody ChatQueryRequest chatQueryRequest) {
        ThrowUtils.throwIf(chatQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(chatService.getListPage(chatQueryRequest));
    }

    /**
     * 分页获取聊天列表（封装类）
     *
     * @param chatQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    @ApiOperation(value = "分页获取聊天列表（封装类）")
    public ApiResponse<Page<ChatVO>> listChatVOByPage(@RequestBody ChatQueryRequest chatQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(chatQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(chatService.handlePaginationAndValidation(chatQueryRequest, request));
    }

    /**
     * 分页获取当前登录用户创建的聊天列表
     *
     * @param chatQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    @ApiOperation(value = "分页获取当前登录用户创建的聊天列表")
    public ApiResponse<Page<ChatVO>> listMyChatVOByPage(@RequestBody ChatQueryRequest chatQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(chatQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        chatQueryRequest.setCreateBy(loginUser.getId());
        return ApiResult.success(chatService.handlePaginationAndValidation(chatQueryRequest, request));
    }

    // endregion

}
