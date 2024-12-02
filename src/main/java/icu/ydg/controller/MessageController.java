package icu.ydg.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import icu.ydg.annotation.Verify;
import icu.ydg.common.ApiResponse;
import icu.ydg.common.ApiResult;
import icu.ydg.common.ErrorCode;
import icu.ydg.constant.UserConstant;
import icu.ydg.exception.BusinessException;
import icu.ydg.model.domain.Message;
import icu.ydg.model.domain.Post;
import icu.ydg.model.domain.User;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.chat.PrivateChatVO;
import icu.ydg.model.dto.message.MessageAddRequest;
import icu.ydg.model.dto.message.MessageQueryRequest;
import icu.ydg.model.dto.message.MessageUpdateRequest;
import icu.ydg.model.vo.message.MessageVO;
import icu.ydg.model.vo.user.UserVO;
import icu.ydg.service.MessageService;
import icu.ydg.service.UserService;
import icu.ydg.utils.ThrowUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 消息接口
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@RestController
@RequestMapping("/message")
@Slf4j
//@Api(tags = "消息接口")
public class MessageController {

    @Resource
    private MessageService messageService;

    @Resource
    private UserService userService;

    /**
     * 获取大厅聊天消息条数
     *
     * @param request 请求
     * @return {@link ApiResponse }<{@link List }<{@link PrivateChatVO }>>
     */
    @GetMapping("/hall/num")
    @ApiOperation(value = "获取大厅未读消息数量")
    public ApiResponse<Map<String, Object>> getUnreadHallNum(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Map<String, Object> num = (Map<String, Object>) messageService.getUnReadHallNum(loginUser.getId(),true);
        return ApiResult.success(num);
    }

    /**
     * 阅读大厅信息
     *
     * @param request 请求
     * @return {@link ApiResponse }<{@link Boolean }>
     */
    @PostMapping("/hall/read")
    public ApiResponse<Boolean> readHallMessage(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Boolean flag = messageService.readHallMessage(loginUser.getId());
        return ApiResult.success(flag);
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
        Integer num = messageService.getUnReadTeamNum(loginUser.getId());
        return ApiResult.success(num);
    }

    /**
     * 阅读私人信息
     *
     * @param request       请求
     * @param readIdRequest 读取id请求
     * @return {@link ApiResponse }<{@link Boolean }>
     */
    @PostMapping("/private/read")
    public ApiResponse<Boolean> readPrivateMessage(HttpServletRequest request, IdRequest readIdRequest) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Boolean flag = messageService.readPrivateMessage(loginUser.getId(), readIdRequest.getId());
        return ApiResult.success(flag);
    }

    /**
     * 阅读团队消息
     *
     * @param request     请求
     * @param teamRequest 团队请求
     * @return {@link ApiResponse }<{@link Boolean }>
     */
    @PostMapping("/team/read")
    public ApiResponse<Boolean> readTeamMessage(HttpServletRequest request, IdRequest teamRequest) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Boolean flag = messageService.readTeamMessage(loginUser.getId(), teamRequest.getId());
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
        Integer unreadNum = messageService.getUnReadPrivateNum(loginUser.getId());
        return ApiResult.success(unreadNum);
    }

    /**
     * 用户有新消息
     *
     * @param request 请求
     * @return {@link ApiResponse }<{@link Boolean }>
     */
    @GetMapping
    @ApiOperation(value = "用户是否有新消息")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Boolean> userHasNewMessage(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Boolean hasNewMessage = messageService.hasNewMessage(loginUser.getId());
        return ApiResult.success(hasNewMessage);
    }

    /**
     * 获取用户新消息数量
     *
     * @param request 请求
     * @return {@link ApiResponse}<{@link Long}>
     */
    @GetMapping("/num")
    @ApiOperation(value = "获取用户新消息数量")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> getUserMessageNum(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        long messageNum = messageService.getMessageNum(loginUser.getId());
        return ApiResult.success(messageNum);
    }

    /**
     * 获取用户帖子评论消息数量
     *
     * @param request 请求
     * @return {@link ApiResponse}<{@link Long}>
     */
    @GetMapping("/post/comment/num")
    @ApiOperation(value = "获取用户帖子评论消息数量")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> getUserCommentMessageNum(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        long messageNum = messageService.getPostCommentNum(loginUser.getId());
        return ApiResult.success(messageNum);
    }

    @GetMapping("/follow/num")
    @ApiOperation(value = "获取用户关注消息数量")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> getUserFollowMessageNum(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        long messageNum = messageService.getFollowNum(loginUser.getId());
        return ApiResult.success(messageNum);
    }

    /**
     * 获取用户帖子点赞消息数量
     *
     * @param request 请求
     * @return {@link ApiResponse}<{@link Long}>
     */
    @GetMapping("/post/num")
    @ApiOperation(value = "获取用户帖子点赞消息数量")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> getUserLikeMessageNum(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        long messageNum = messageService.getPostPraiseNum(loginUser.getId());
        return ApiResult.success(messageNum);
    }

    /**
     * 获取用户帖子
     *
     * @param request     请求
     * @param currentPage 当前页面
     * @return {@link ApiResponse }<{@link Page }<{@link MessageVO }>>
     */
    @GetMapping("/post")
    @ApiOperation(value = "获取用户点赞的帖子")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<List<Post>> getPost(HttpServletRequest request, Long currentPage) {
        User loginUser = userService.getLoginUser(request);
        List<Post> blogVOList = messageService.getUserPost(loginUser.getId());
        return ApiResult.success(blogVOList);
    }

    /**
     * 获取好友应用消息num
     *
     * @param request 请求
     * @return {@link ApiResponse }<{@link Long }>
     */
    @GetMapping("/friend/num")
    @ApiOperation(value = "添加好友申请信息数量")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> getFriendApplyMessageNum(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        long messageNum = messageService.getFriendNum(loginUser.getId());
        return ApiResult.success(messageNum);
    }

    @GetMapping("/friend")
    @ApiOperation(value = "获取用户好友申请")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<List<UserVO>> getFriend(HttpServletRequest request, Long currentPage) {
        User loginUser = userService.getLoginUser(request);
        List<UserVO> blogVOList = messageService.getUserVO(loginUser.getId());
        return ApiResult.success(blogVOList);
    }

    @GetMapping("/fans/num")
    @ApiOperation(value = "用户粉丝数量消息")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> getFansMessageNum(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        long messageNum = messageService.getFansNum(loginUser.getId());
        return ApiResult.success(messageNum);
    }

    @GetMapping("/fans")
    @ApiOperation(value = "获取用户粉丝消息")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<List<UserVO>> getFans(HttpServletRequest request, Long currentPage) {
        User loginUser = userService.getLoginUser(request);
        List<UserVO> blogVOList = messageService.getFans(loginUser.getId());
        return ApiResult.success(blogVOList);
    }

    // todo 聊天的信息和内容


    // region 增删改查

    /**
     * 创建消息
     *
     * @param messageAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "创建消息")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> addMessage(@RequestBody MessageAddRequest messageAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(messageAddRequest == null, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(messageService.addMessage(messageAddRequest, request));
    }

    /**
     * 删除消息
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @ApiOperation(value = "删除消息")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> deleteMessage(@RequestBody IdRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(messageService.deleteMessage(deleteRequest, request), "删除成功！");
    }

    /**
     * 更新消息（仅管理员可用）
     *
     * @param messageUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @ApiOperation(value = "更新消息（需要 user 权限）")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> updateMessage(@RequestBody MessageUpdateRequest messageUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(messageUpdateRequest == null || messageUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(messageService.updateMessage(messageUpdateRequest, request), "更新成功！");
    }

    /**
     * 根据 id 获取消息（封装类）
     *
     * @param idRequest
     * @return
     */
    @GetMapping("/get/vo")
    @ApiOperation(value = "根据 id 获取消息（封装类）")
    public ApiResponse<MessageVO> getMessageVOById(IdRequest idRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(idRequest == null || idRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 获取封装类
        return ApiResult.success(messageService.getMessageVO(idRequest, request));
    }

    /**
     * 分页获取消息列表（仅管理员可用）
     *
     * @param messageQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @ApiOperation(value = "分页获取消息列表（仅管理员可用）")
    @Verify(checkAuth = UserConstant.ADMIN_ROLE)
    public ApiResponse<Page<Message>> listMessageByPage(@RequestBody MessageQueryRequest messageQueryRequest) {
        ThrowUtils.throwIf(messageQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(messageService.getListPage(messageQueryRequest));
    }

    /**
     * 分页获取消息列表（封装类）
     *
     * @param messageQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    @ApiOperation(value = "分页获取消息列表（封装类）")
    public ApiResponse<Page<MessageVO>> listMessageVOByPage(@RequestBody MessageQueryRequest messageQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(messageQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(messageService.handlePaginationAndValidation(messageQueryRequest, request));
    }

    /**
     * 分页获取当前登录用户创建的消息列表
     *
     * @param messageQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    @ApiOperation(value = "分页获取当前登录用户创建的消息列表")
    public ApiResponse<Page<MessageVO>> listMyMessageVOByPage(@RequestBody MessageQueryRequest messageQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(messageQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        messageQueryRequest.setCreateBy(loginUser.getId());
        return ApiResult.success(messageService.handlePaginationAndValidation(messageQueryRequest, request));
    }

}
