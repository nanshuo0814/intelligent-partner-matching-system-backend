package icu.ydg.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.ydg.common.ErrorCode;
import icu.ydg.constant.PageConstant;
import icu.ydg.constant.UserConstant;
import icu.ydg.exception.BusinessException;
import icu.ydg.mapper.PostCollectMapper;
import icu.ydg.mapper.PostMapper;
import icu.ydg.mapper.PostPraiseMapper;
import icu.ydg.model.domain.Post;
import icu.ydg.model.domain.PostCollect;
import icu.ydg.model.domain.PostPraise;
import icu.ydg.model.domain.User;
import icu.ydg.model.dto.IdBatchRequest;
import icu.ydg.model.dto.post.PostQueryRequest;
import icu.ydg.model.dto.post.PostUpdateRequest;
import icu.ydg.model.enums.post.PostStatusEnums;
import icu.ydg.model.enums.sort.PostSortFieldEnums;
import icu.ydg.model.vo.post.PostVO;
import icu.ydg.model.vo.user.UserVO;
import icu.ydg.service.PostService;
import icu.ydg.service.UserService;
import icu.ydg.utils.SqlUtils;
import icu.ydg.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 帖子服务实现类
 *
 * @author 袁德光
 * @date 2024/07/26
 */
@Slf4j
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post>
        implements PostService {

    /**
     * 帖子标题长度
     */
    public static final int POST_TITLE_LENGTH = 80;
    /**
     * 帖子内容长度
     */
    public static final int POST_CONTENT_LENGTH = 8192;

    @Resource
    private UserService userService;
    @Resource
    private PostPraiseMapper postPraiseMapper;
    @Resource
    private PostCollectMapper postCollectMapper;
    @Autowired
    private PostMapper postMapper;

    /**
     * 有效帖子
     *
     * @param post post
     * @param add  添加
     */
    @Override
    public void validPost(Post post, boolean add) {
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String title = post.getTitle();
        String content = post.getContent();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(title, content), ErrorCode.PARAMS_ERROR, "标题和内容不能为空");
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(title) && title.length() > POST_TITLE_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        if (StringUtils.isNotBlank(content) && content.length() > POST_CONTENT_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
        }
    }

    /**
     * 获取帖子封装
     *
     * @param post    post
     * @param request 请求
     * @return {@code PostVO}
     */
    @Override
    public PostVO getPostVO(Post post, HttpServletRequest request) {
        PostVO postVO = PostVO.objToVo(post);
        long postId = post.getId();
        // 1. 关联查询用户信息
        Long userId = post.getCreateBy();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        postVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            LambdaQueryWrapper<PostPraise> postThumbQueryWrapper = new LambdaQueryWrapper<>();
            postThumbQueryWrapper.in(PostPraise::getPostId, postId);
            postThumbQueryWrapper.eq(PostPraise::getCreateBy, loginUser.getId());
            PostPraise postThumb = postPraiseMapper.selectOne(postThumbQueryWrapper);
            // 获取收藏
            LambdaQueryWrapper<PostCollect> postFavourQueryWrapper = new LambdaQueryWrapper<>();
            postFavourQueryWrapper.in(PostCollect::getPostId, postId);
            postFavourQueryWrapper.eq(PostCollect::getCreateBy, loginUser.getId());
            PostCollect postFavour = postCollectMapper.selectOne(postFavourQueryWrapper);
        }
        return postVO;
    }

    /**
     * 获取查询包装器
     *
     * @param postQueryRequest post查询请求
     * @return {@code QueryWrapper<Post>}
     */
    @Override
    public LambdaQueryWrapper<Post> getQueryWrapper(PostQueryRequest postQueryRequest) {
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        if (postQueryRequest == null) {
            return queryWrapper;
        }
        String searchText = postQueryRequest.getSearchText();
        String sortField = postQueryRequest.getSortField();
        String sortOrder = postQueryRequest.getSortOrder();
        Long id = postQueryRequest.getId();
        String title = postQueryRequest.getTitle();
        String content = postQueryRequest.getContent();
        Long userId = postQueryRequest.getCreateBy();
        Long notId = postQueryRequest.getNotId();
        Integer status = postQueryRequest.getStatus();
        // 拼接查询条件
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like(Post::getTitle, searchText).or().like(Post::getContent, searchText));
        }
        queryWrapper.like(StringUtils.isNotBlank(title), Post::getTitle, title);
        queryWrapper.like(StringUtils.isNotBlank(content), Post::getContent, content);
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), Post::getId, notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), Post::getId, id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(status), Post::getStatus, status);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), Post::getCreateBy, userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(PageConstant.SORT_ORDER_ASC),
                isSortField(sortField));
        return queryWrapper;
    }

    /**
     * 获取post vo页面
     *
     * @param postPage  帖子页面
     * @param loginUser 登录用户
     * @return {@link Page }<{@link PostVO }>
     */
    @Override
    public Page<PostVO> getPostVoPage(Page<Post> postPage, User loginUser) {
        List<Post> postList = postPage.getRecords();
        Page<PostVO> postVoPage = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        if (CollUtil.isEmpty(postList)) {
            return postVoPage;
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = postList.stream().map(Post::getCreateBy).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> postIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> postIdHasFavourMap = new HashMap<>();
        if (loginUser != null) {
            Set<Long> postIdSet = postList.stream().map(Post::getId).collect(Collectors.toSet());
            // 获取点赞
            LambdaQueryWrapper<PostPraise> postThumbQueryWrapper = new LambdaQueryWrapper<>();
            postThumbQueryWrapper.in(PostPraise::getId, postIdSet);
            postThumbQueryWrapper.eq(PostPraise::getCreateBy, loginUser.getId());
            List<PostPraise> postPostThumbList = postPraiseMapper.selectList(postThumbQueryWrapper);
            postPostThumbList.forEach(postPostThumb -> postIdHasThumbMap.put(postPostThumb.getPostId(), true));
            // 获取收藏
            LambdaQueryWrapper<PostCollect> postFavourQueryWrapper = new LambdaQueryWrapper<>();
            postFavourQueryWrapper.in(PostCollect::getPostId, postIdSet);
            postFavourQueryWrapper.eq(PostCollect::getCreateBy, loginUser.getId());
            List<PostCollect> postFavourList = postCollectMapper.selectList(postFavourQueryWrapper);
            postFavourList.forEach(postFavour -> postIdHasFavourMap.put(postFavour.getPostId(), true));
        }
        // 填充信息
        List<PostVO> postVOList = postList.stream().map(post -> {
            PostVO postVO = PostVO.objToVo(post);
            Long userId = post.getCreateBy();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            postVO.setUser(userService.getUserVO(user));
            return postVO;
        }).collect(Collectors.toList());
        postVoPage.setRecords(postVOList);
        return postVoPage;
    }

    @Override
    public long updatePost(PostUpdateRequest postUpdateRequest, HttpServletRequest request) {
        // 本人和管理员可修改
        User user = userService.getLoginUser(request);
        if (!UserConstant.ADMIN_ROLE.equals(user.getUserRole()) && !Objects.equals(user.getId(), postUpdateRequest.getCreateBy())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限修改帖子内容");
        }
        long id = postUpdateRequest.getId();
        // 获取到帖子
        Post oldPost = postMapper.selectById(id);
        ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR, "帖子不存在");
        Optional.ofNullable(postUpdateRequest.getTitle())
                .filter(StringUtils::isNotBlank)
                .ifPresent(oldPost::setTitle);
        Optional.ofNullable(postUpdateRequest.getContent())
                .filter(StringUtils::isNotBlank)
                .ifPresent(oldPost::setContent);
        Optional.ofNullable(postUpdateRequest.getCoverImage())
                .filter(StringUtils::isNotBlank)
                .ifPresent(oldPost::setCoverImage);
        Integer status = postUpdateRequest.getStatus();
        if (status != null) {
            PostStatusEnums enumByValue = PostStatusEnums.getEnumByValue(status);
            if (enumByValue == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "帖子状态错误！");
            }
            oldPost.setStatus(status);
        }
        // 参数校验
        validPost(oldPost, false);
        // 更新
        postMapper.updateById(oldPost);
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shenHeBatchStatus(IdBatchRequest deleteRequest, HttpServletRequest request) {
        List<Long> ids = deleteRequest.getIds();
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("审核的帖子ID列表不能为空");
        }
        // 执行批量审核
        for (Long id : ids) {
            Post oldPost = postMapper.selectById(id);
            if (oldPost == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "帖子不存在");
            }
            if (oldPost.getStatus().equals(PostStatusEnums.PASS.getValue())) {
                oldPost.setStatus(PostStatusEnums.NO_PASS.getValue());
            } else if (oldPost.getStatus().equals(PostStatusEnums.NO_PASS.getValue())) {
                oldPost.setStatus(PostStatusEnums.PASS.getValue());
            } else {
                oldPost.setStatus(PostStatusEnums.PASS.getValue());
            }
            postMapper.updateById(oldPost);
        }
    }

    /**
     * 是否为排序字段
     *
     * @param sortField 排序字段
     * @return {@code SFunction<Post, ?>}
     */
    private SFunction<Post, ?> isSortField(String sortField) {
        if (Objects.equals(sortField, "")) {
            sortField = PostSortFieldEnums.UPDATE_TIME.name();
        }
        if (SqlUtils.validSortField(sortField)) {
            return PostSortFieldEnums.fromString(sortField)
                    .map(PostSortFieldEnums::getFieldGetter)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "错误的排序字段"));
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "排序字段无效");
        }
    }


}




