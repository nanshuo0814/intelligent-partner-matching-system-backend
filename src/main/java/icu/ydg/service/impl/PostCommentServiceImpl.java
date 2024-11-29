package icu.ydg.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.ydg.common.ErrorCode;
import icu.ydg.constant.PageConstant;
import icu.ydg.constant.RedisKeyConstant;
import icu.ydg.constant.UserConstant;
import icu.ydg.exception.BusinessException;
import icu.ydg.mapper.PostCommentMapper;
import icu.ydg.mapper.PostMapper;
import icu.ydg.model.domain.Post;
import icu.ydg.model.domain.PostComment;
import icu.ydg.model.domain.User;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.postComment.PostCommentAddRequest;
import icu.ydg.model.dto.postComment.PostCommentQueryRequest;
import icu.ydg.model.dto.postComment.PostCommentUpdateRequest;
import icu.ydg.model.enums.sort.PostCommentSortFieldEnums;
import icu.ydg.model.vo.postComment.PostCommentVO;
import icu.ydg.model.vo.user.UserVO;
import icu.ydg.service.PostCommentService;
import icu.ydg.service.PostService;
import icu.ydg.service.UserService;
import icu.ydg.utils.SqlUtils;
import icu.ydg.utils.ThrowUtils;
import icu.ydg.utils.redis.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 帖子评论服务实现
 *
 * @author 袁德光
 * @Date 2024/11/02
 */
@Service
@Slf4j
public class PostCommentServiceImpl extends ServiceImpl<PostCommentMapper, PostComment> implements PostCommentService {

    @Resource
    private UserService userService;
    @Resource
    private PostCommentMapper postCommentMapper;
    @Resource
    private PostService postService;
    @Autowired
    private PostMapper postMapper;
    @Resource
    private RedisUtils redisUtils;
    // todo 如果后续需要点赞或收藏可自行添加，参考 Post 帖子表有现成的代码

    /**
     * 添加帖子评论
     *
     * @param postCommentAddRequest 发表评论添加请求
     * @param request               请求
     * @return long
     */
    @Override
    public long addPostComment(PostCommentAddRequest postCommentAddRequest, HttpServletRequest request) {
        // todo 在此处将实体类和 DTO 进行转换
        PostComment postComment = new PostComment();
        BeanUtils.copyProperties(postCommentAddRequest, postComment);
        // 数据校验
        validPostComment(postComment, true);
        // todo 填充值
        String content = postCommentAddRequest.getContent();
        Long postId = postCommentAddRequest.getPostId();
        ThrowUtils.throwIf(postId == null, ErrorCode.NOT_FOUND_ERROR, "帖子不存在");
        Post byId = postService.getById(postId);
        ThrowUtils.throwIf(byId == null, ErrorCode.NOT_FOUND_ERROR, "帖子不存在");
        Long parentId = postCommentAddRequest.getParentId();
        Long answerId = postCommentAddRequest.getAnswerId();
        Long toUserId = postCommentAddRequest.getToUserId();
        // 设置值
        postComment.setContent(content);
        postComment.setPostId(postId);
        postComment.setParentId(parentId);
        postComment.setAnswerId(answerId);
        postComment.setToUserId(toUserId);

        // 写入数据库
        int insert = postCommentMapper.insert(postComment);
        ThrowUtils.throwIf(insert <= 0, ErrorCode.OPERATION_ERROR);
        redisUtils.incr(RedisKeyConstant.MESSAGE_POST_COMMENT_NUM_KEY + postComment.getToUserId(), 1);
        // 返回新写入的数据 id
        long newPostCommentId = postComment.getId();
        // 返回新写入的数据 id
        return postComment.getId();
    }

    /**
     * 删除帖子评论
     *
     * @param deleteRequest 删除请求
     * @param request       请求
     * @return long
     */
    @Override
    public long deletePostComment(IdRequest deleteRequest, HttpServletRequest request) {
        long id = deleteRequest.getId();
        onlyMeOrAdministratorCanDo(request, id);
         // 若删除的是一级评论，则删除关联的二级评论
        PostComment postComment = postCommentMapper.selectById(id);
         // 根据 parentId 为 null 判断是否为一级评论
        if (postComment != null && postComment.getParentId() == null) {
            LambdaQueryWrapper<PostComment> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PostComment::getParentId, id);
            List<PostComment> postComments = postCommentMapper.selectList(queryWrapper);
            if (postComments != null && !postComments.isEmpty()) {
                Set<Long> ids = postComments.stream().map(PostComment::getId).collect(Collectors.toSet());
                postCommentMapper.deleteBatchIds(ids);
            }
        }
        ThrowUtils.throwIf(postCommentMapper.deleteById(id) <= 0, ErrorCode.OPERATION_ERROR);
        assert postComment != null;
        redisUtils.decr(RedisKeyConstant.MESSAGE_POST_COMMENT_NUM_KEY + postComment.getToUserId(), 1);
        return id;
    }

    /**
     * 校验数据
     *
     * @param postComment
     * @param add         对创建的数据进行校验
     */
    @Override
    public void validPostComment(PostComment postComment, boolean add) {
        ThrowUtils.throwIf(postComment == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值，自行修改为正确的属性
        //String title = postComment.getTitle();

        // 创建数据时，参数不能为空
        if (add) {
            // todo 补充参数不为空校验规则
            //ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR);
        }
        // 修改数据时，有参数则校验
        // todo 补充参数其他校验规则
        //if (StringUtils.isNotBlank(title)) {
        //    ThrowUtils.throwIf(title.length() > 80, ErrorCode.PARAMS_ERROR, "标题过长");
        //}
    }

    /**
     * 获取查询条件
     *
     * @param postCommentQueryRequest
     * @return
     */
    @Override
    public LambdaQueryWrapper<PostComment> getQueryWrapper(PostCommentQueryRequest postCommentQueryRequest) {
        LambdaQueryWrapper<PostComment> queryWrapper = new LambdaQueryWrapper<>();
        if (postCommentQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = postCommentQueryRequest.getId();
        Long notId = postCommentQueryRequest.getNotId();
        Long postId = postCommentQueryRequest.getPostId();
        Long parentId = postCommentQueryRequest.getParentId();
        Long answerId = postCommentQueryRequest.getAnswerId();
        Long toUserId = postCommentQueryRequest.getToUserId();
        //String title = postCommentQueryRequest.getTitle();
        String content = postCommentQueryRequest.getContent();
        //String searchText = postCommentQueryRequest.getSearchText();
        String sortField = postCommentQueryRequest.getSortField();
        String sortOrder = postCommentQueryRequest.getSortOrder();
        Long userId = postCommentQueryRequest.getCreateBy();
        //List<String> tagList = postCommentQueryRequest.getTags();
        // todo 补充需要的查询条件
        // 从多字段中搜索
        //if (StringUtils.isNotBlank(searchText)) {
        //    // todo 需要拼接查询条件
        //    queryWrapper.and(qw -> qw.like(PostComment::getTitle, searchText).or().like(PostComment::getContent, searchText));
        //}
        // 模糊查询
        //queryWrapper.like(StringUtils.isNotBlank(title), PostComment::getTitle, title);
        queryWrapper.like(StringUtils.isNotBlank(content), PostComment::getContent, content);
        // JSON 数组查询
        //if (CollUtil.isNotEmpty(tagList)) {
        //    for (String tag : tagList) {
        //        queryWrapper.like(PostComment::getTags, "\"" + tag + "\"");
        //    }
        //}
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), PostComment::getId, notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), PostComment::getId, id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(postId), PostComment::getPostId, postId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), PostComment::getCreateBy, userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(parentId), PostComment::getParentId, parentId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(answerId), PostComment::getAnswerId, answerId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(toUserId), PostComment::getToUserId, toUserId);
        // 默认排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(PageConstant.SORT_ORDER_ASC),
                isSortField(sortField));
        return queryWrapper;
    }

    /**
     * 是否为排序字段
     *
     * @param sortField 排序字段
     * @return {@code SFunction<Post, ?>}
     */
    private SFunction<PostComment, ?> isSortField(String sortField) {
        if (Objects.equals(sortField, "")) {
            sortField = PostCommentSortFieldEnums.UPDATE_TIME.name();
        }
        if (SqlUtils.validSortField(sortField)) {
            return PostCommentSortFieldEnums.fromString(sortField).map(PostCommentSortFieldEnums::getFieldGetter).orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "排序字段非法"));
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "排序字段无效，未知错误");
        }
    }

    /**
     * 获取帖子评论封装
     *
     * @param idRequest
     * @param request
     * @return
     */
    @Override
    public PostCommentVO getPostCommentVO(IdRequest idRequest, HttpServletRequest request) {
        long id = idRequest.getId();
        // 查询数据库
        PostComment postComment = postCommentMapper.selectById(id);
        ThrowUtils.throwIf(postComment == null, ErrorCode.NOT_FOUND_ERROR);
        // 对象转封装类
        PostCommentVO postCommentVO = PostCommentVO.objToVo(postComment);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = postComment.getCreateBy();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        postCommentVO.setUser(userVO);
        return postCommentVO;
    }

    /**
     * “获取列表” 页
     *
     * @param postCommentQueryRequest
     * @return {@link Page }<{@link PostComment }>
     */
    @Override
    public Page<PostComment> getListPage(PostCommentQueryRequest postCommentQueryRequest) {
        long current = postCommentQueryRequest.getCurrent();
        long size = postCommentQueryRequest.getPageSize();
        if (size == 0L) {
            size = PageConstant.PAGE_SIZE;
        }
        if (current == 0L) {
            current = PageConstant.CURRENT_PAGE;
        }
        // 查询数据库
        return this.page(new Page<>(current, size), this.getQueryWrapper(postCommentQueryRequest));
    }

    /**
     * 分页获取帖子评论封装
     *
     * @param postCommentPage
     * @param request
     * @return
     */
    @Override
    public Page<PostCommentVO> getPostCommentVOPage(Page<PostComment> postCommentPage, HttpServletRequest request) {
        List<PostComment> postCommentList = postCommentPage.getRecords();
        Page<PostCommentVO> postCommentVOPage = new Page<>(postCommentPage.getCurrent(), postCommentPage.getSize(), postCommentPage.getTotal());
        if (CollUtil.isEmpty(postCommentList)) {
            return postCommentVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PostCommentVO> postCommentVOList = postCommentList.stream().map(postComment -> {
            return PostCommentVO.objToVo(postComment);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = postCommentList.stream().map(PostComment::getCreateBy).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        //Map<Long, Boolean> postCommentIdHasPraiseMap = new HashMap<>();
        //Map<Long, Boolean> postCommentIdHasCollectMap = new HashMap<>();
        //User loginUser = userService.getLoginUserPermitNull(request);
        //if (loginUser != null) {
        //    Set<Long> postCommentIdSet = postCommentList.stream().map(PostComment::getId).collect(Collectors.toSet());
        //    loginUser = userService.getLoginUser(request);
        //    // 获取点赞
        //    LambdaQueryWrapper<PostCommentPraise> postCommentPraiseQueryWrapper = new LambdaQueryWrapper<>();
        //    postCommentPraiseQueryWrapper.in(PostCommentPraise::getId, postCommentIdSet);
        //    postCommentPraiseQueryWrapper.eq(PostCommentPraise::getCreateBy, loginUser.getId());
        //    List<PostCommentPraise> postCommentPostCommentPraiseList = postCommentThumbMapper.selectList(postCommentThumbQueryWrapper);
        //    postCommentPostCommentThumbList.forEach(postCommentPostCommentPraise -> postCommentIdHasPraiseMap.put(postCommentPostCommentPraise.getPostCommentId(), true));
        //    // 获取收藏
        //    LambdaQueryWrapper<PostCommentCollect> postCommentCollectQueryWrapper = new LambdaQueryWrapper<>();
        //    postCommentCollectQueryWrapper.in(PostCommentCollect::getId, postCommentIdSet);
        //    postCommentCollectQueryWrapper.eq(PostCommentCollect::getCreateBy, loginUser.getId());
        //    List<PostCommentCollect> postCommentCollectList = postCommentCollectMapper.selectList(postCommentCollectQueryWrapper);
        //    postCommentCollectList.forEach(postCommentCollect -> postCommentIdHasCollectMap.put(postCommentCollect.getPostCommentId(), true));
        //}
        // 填充创建用户信息
        postCommentVOList.forEach(postCommentVO -> {
            Long userId = postCommentVO.getCreateBy();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            postCommentVO.setUser(userService.getUserVO(user));
        });
        // endregion
        // 填充回复用户信息
        Set<Long> answerUser = postCommentList.stream().map(PostComment::getToUserId).collect(Collectors.toSet());
        Map<Long, List<User>> answerMap = userService.listByIds(answerUser).stream()
                .collect(Collectors.groupingBy(User::getId));
        postCommentVOList.forEach(postCommentVO -> {
            Long answerId = postCommentVO.getToUserId();
            User user = null;
            if (answerMap.containsKey(answerId)) {
                user = answerMap.get(answerId).get(0);
            }
            postCommentVO.setToUser(userService.getUserVO(user));
        });
        postCommentVOPage.setRecords(postCommentVOList);
        return postCommentVOPage;
    }

    /**
     * 更新帖子评论
     *
     * @param postCommentUpdateRequest 更新后请求
     * @param request                  请求
     * @return long
     */
    @Override
    public long updatePostComment(PostCommentUpdateRequest postCommentUpdateRequest, HttpServletRequest request) {
        // 管理员可修改
        User user = userService.getLoginUser(request);
        if (!UserConstant.ADMIN_ROLE.equals(user.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        long id = postCommentUpdateRequest.getId();
        // 获取数据
        PostComment oldPostComment = postCommentMapper.selectById(id);
        ThrowUtils.throwIf(oldPostComment == null, ErrorCode.NOT_FOUND_ERROR);
        // todo 设置值
        oldPostComment.setContent(postCommentUpdateRequest.getContent());
        // 参数校验
        validPostComment(oldPostComment, false);
        // 更新
        postCommentMapper.updateById(oldPostComment);
        return id;
    }

    /**
     * 处理分页和验证
     *
     * @param postCommentQueryRequest 帖子评论查询请求
     * @param request                 请求
     * @return {@code Page<PostCommentVO>}
     */
    @Override
    public Page<PostCommentVO> handlePaginationAndValidation(PostCommentQueryRequest postCommentQueryRequest, HttpServletRequest request) {
        long current = postCommentQueryRequest.getCurrent();
        long size = postCommentQueryRequest.getPageSize();
        if (size == 0L) {
            size = PageConstant.PAGE_SIZE;
        }
        if (current == 0L) {
            current = PageConstant.CURRENT_PAGE;
        }
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<PostComment> postCommentPage = this.page(new Page<>(current, size), this.getQueryWrapper(postCommentQueryRequest));
        return this.getPostCommentVOPage(postCommentPage, request);
    }

    /**
     * 只有本人或管理员可以执行
     *
     * @param request 请求
     * @param id      id
     */
    @Override
    public void onlyMeOrAdministratorCanDo(HttpServletRequest request, Long id) {
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        PostComment oldPostComment = postCommentMapper.selectById(id);
        ThrowUtils.throwIf(oldPostComment == null, ErrorCode.NOT_FOUND_ERROR);
        // 帖子不是登录用户的并且删除的不是自己评论的评论
        Post byId = postService.getById(oldPostComment.getPostId());
        if (!oldPostComment.getCreateBy().equals(loginUser.getId()) && !byId.getCreateBy().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "你当前暂无该权限！");
        }
        // 仅本人或管理员可操作（这里假设"编辑"和"删除"操作的权限是一样的）
        if (!oldPostComment.getCreateBy().equals(loginUser.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "你当前暂无该权限！");
        }
    }

    /**
     * 列表页vo子项
     *
     * @param postCommentQueryRequest 发表评论查询请求
     * @param request                 请求
     * @return {@link Page }<{@link PostCommentVO }>
     */
    @Override
    public Page<PostCommentVO> listPageVoChildren(PostCommentQueryRequest postCommentQueryRequest, HttpServletRequest request) {
        // 1. 参数校验
        long current = postCommentQueryRequest.getCurrent();
        long size = postCommentQueryRequest.getPageSize();
        if (size == 0L) {
            size = 5;
        }
        if (current == 0L) {
            current = PageConstant.CURRENT_PAGE;
        }
        // 限制爬虫
        ThrowUtils.throwIf(size > 6, ErrorCode.PARAMS_ERROR);
        // 2. 创建分页对象
        Page<PostComment> page = new Page<>(current, size);
        // 3. 查询一级评论
        // todo 可优化性能的地方，可在评论表添加一个 hasChildrenComment 字段来知道是否有二级评论，有则查询，无则跳过，减少无效的二级评论查询，缩短查询时间
        LambdaQueryWrapper<PostComment> queryWrapper = new LambdaQueryWrapper<>();
        // 一级评论：parentId为null
        queryWrapper.isNull(PostComment::getParentId);
        // 筛选指定帖子的评论
        queryWrapper.eq(PostComment::getPostId, postCommentQueryRequest.getPostId());
        // 按创建时间降序排列
        queryWrapper.orderByDesc(PostComment::getCreateTime);
        // 执行分页查询
        Page<PostComment> postCommentPage = postCommentMapper.selectPage(page, queryWrapper);
        // 4. 获取评论VO分页对象
        List<PostCommentVO> commentVOList = postCommentPage.getRecords().stream()
                .map(comment -> {
                    PostCommentVO postCommentVO = PostCommentVO.objToVo(comment);
                    postCommentVO.setChildren(loadChildren(comment.getId()));
                    postCommentVO.setUser(UserVO.objToVo(userService.getById(comment.getCreateBy())));
                    postCommentVO.setToUser(UserVO.objToVo(userService.getById(comment.getToUserId())));
                    return postCommentVO;
                }).collect(Collectors.toList());
        Page<PostCommentVO> resultPage = new Page<>();
        resultPage.setCurrent(postCommentPage.getCurrent());
        resultPage.setSize(postCommentPage.getSize());
        resultPage.setTotal(postCommentPage.getTotal());
        resultPage.setRecords(commentVOList);
        return resultPage;
    }

    /**
     * 加载指定评论的子评论
     *
     * @param parentId 一级评论的 ID
     * @return 子评论列表
     */
    private List<PostCommentVO> loadChildren(Long parentId) {
        // 查询子评论
        LambdaQueryWrapper<PostComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PostComment::getParentId, parentId);
        queryWrapper.orderByDesc(PostComment::getCreateTime);
        List<PostComment> children = postCommentMapper.selectList(queryWrapper);
        // 转换为 VO 对象
        return children.stream()
                //.map(PostCommentVO::objToVo)
                .map(comment -> {
                    PostCommentVO postCommentVO = PostCommentVO.objToVo(comment);
                    //postCommentVO.setChildren(loadChildren(comment.getId()));
                    postCommentVO.setUser(UserVO.objToVo(userService.getById(comment.getCreateBy())));
                    postCommentVO.setToUser(UserVO.objToVo(userService.getById(comment.getToUserId())));
                    return postCommentVO;
                })
                .collect(Collectors.toList());
    }

}
