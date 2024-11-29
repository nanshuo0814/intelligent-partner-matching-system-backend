package icu.ydg.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.ydg.common.ErrorCode;
import icu.ydg.constant.PageConstant;
import icu.ydg.constant.UserConstant;
import icu.ydg.exception.BusinessException;
import icu.ydg.mapper.TagMapper;
import icu.ydg.model.domain.Tag;
import icu.ydg.model.domain.User;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.tag.TagAddRequest;
import icu.ydg.model.dto.tag.TagQueryRequest;
import icu.ydg.model.dto.tag.TagUpdateRequest;
import icu.ydg.model.enums.sort.TagSortFieldEnums;
import icu.ydg.model.vo.tag.TagVO;
import icu.ydg.model.vo.user.UserVO;
import icu.ydg.service.TagService;
import icu.ydg.service.UserService;
import icu.ydg.utils.SqlUtils;
import icu.ydg.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 标签服务实现
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@Service
@Slf4j
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Resource
    @Lazy
    private UserService userService;
    @Resource
    private TagMapper tagMapper;

    /**
     * 添加标签
     *
     * @param tagAddRequest 发表标签添加请求
     * @param request       请求
     * @return long
     */
    @Override
    public long addTag(TagAddRequest tagAddRequest, HttpServletRequest request) {
        Tag tag = new Tag();
        BeanUtils.copyProperties(tagAddRequest, tag);
        // 数据校验
        validTag(tag, true);
        // todo 填充值
        User loginUser = userService.getLoginUser(request);
        tag.setCreateBy(loginUser.getId());
        // 写入数据库
        int insert = tagMapper.insert(tag);
        ThrowUtils.throwIf(insert <= 0, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newTagId = tag.getId();
        // 返回新写入的数据 id
        return tag.getId();
    }

    /**
     * 删除标签
     *
     * @param deleteRequest 删除请求
     * @param request       请求
     * @return long
     */
    @Override
    public long deleteTag(IdRequest deleteRequest, HttpServletRequest request) {
        long id = deleteRequest.getId();
        onlyMeOrAdministratorCanDo(request, id);
        ThrowUtils.throwIf(tagMapper.deleteById(id) <= 0, ErrorCode.OPERATION_ERROR);
        return id;
    }

    /**
     * 校验数据
     *
     * @param tag
     * @param add 对创建的数据进行校验
     */
    @Override
    public void validTag(Tag tag, boolean add) {
        ThrowUtils.throwIf(tag == null, ErrorCode.PARAMS_ERROR);
        String tagName = tag.getTagName();
        String category = tag.getCategory();
        ThrowUtils.throwIf(ObjectUtils.isEmpty(tagName), ErrorCode.PARAMS_ERROR, "标签名称不能为空");
        ThrowUtils.throwIf(ObjectUtils.isEmpty(category), ErrorCode.PARAMS_ERROR, "标签分类不能为空");
        // 校验分类和标签是否都存在
        Long count = tagMapper
                .selectCount(new LambdaQueryWrapper<Tag>().eq(Tag::getTagName, tagName).eq(Tag::getCategory, category));
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "标签已存在");
        if (!add) {
            // 修改
            Long id = tag.getId();
            ThrowUtils.throwIf(ObjectUtils.isEmpty(id) || id <= 0, ErrorCode.PARAMS_ERROR, "id非法");
        }
    }

    @Override
    public List<String> getAllCategories() {
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DISTINCT category");
        return tagMapper.selectObjs(queryWrapper).stream().map(obj -> (String) obj).collect(Collectors.toList());
    }

    /**
     * 获取查询条件
     *
     * @param tagQueryRequest
     * @return
     */
    @Override
    public LambdaQueryWrapper<Tag> getQueryWrapper(TagQueryRequest tagQueryRequest) {
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        if (tagQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = tagQueryRequest.getId();
        Long notId = tagQueryRequest.getNotId();
        String searchText = tagQueryRequest.getSearchText();
        String sortField = tagQueryRequest.getSortField();
        String sortOrder = tagQueryRequest.getSortOrder();
        Long userId = tagQueryRequest.getCreateBy();
        String tagName = tagQueryRequest.getTagName();
        String category = tagQueryRequest.getCategory();
        // 从多字段中搜索
        //if (StringUtils.isNotBlank(searchText)) {
        //    // todo 需要拼接查询条件
        //    queryWrapper.and(qw -> qw.like(Tag::getTitle, searchText).or().like(Tag::getContent, searchText));
        //}
        // 模糊查询
        //queryWrapper.like(StringUtils.isNotBlank(title), Tag::getTitle, title);
        //queryWrapper.like(StringUtils.isNotBlank(content), Tag::getContent, content);
        // 精确查询
        queryWrapper.like(ObjectUtils.isNotEmpty(tagName),Tag::getTagName, tagName);
        queryWrapper.eq(ObjectUtils.isNotEmpty(category),Tag::getCategory, category);
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), Tag::getId, notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), Tag::getId, id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), Tag::getCreateBy, userId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(PageConstant.SORT_ORDER_ASC), isSortField(sortField));
        return queryWrapper;
    }

    /**
     * 是否为排序字段
     *
     * @param sortField 排序字段
     * @return {@code SFunction<Tag, ?>}
     */
    private SFunction<Tag, ?> isSortField(String sortField) {
        if (Objects.equals(sortField, "")) {
            sortField = TagSortFieldEnums.UPDATE_TIME.name();
        }
        if (SqlUtils.validSortField(sortField)) {
            return TagSortFieldEnums.fromString(sortField).map(TagSortFieldEnums::getFieldGetter).orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "排序字段非法"));
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "排序字段无效，未知错误");
        }
    }

    /**
     * 获取标签封装
     *
     * @param idRequest
     * @param request
     * @return
     */
    @Override
    public TagVO getTagVO(IdRequest idRequest, HttpServletRequest request) {
        long id = idRequest.getId();
        // 查询数据库
        Tag tag = tagMapper.selectById(id);
        ThrowUtils.throwIf(tag == null, ErrorCode.NOT_FOUND_ERROR);
        // 对象转封装类
        TagVO tagVO = TagVO.objToVo(tag);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = tag.getCreateBy();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        tagVO.setUser(userVO);
        return tagVO;
    }

    /**
     * “获取列表” 页
     *
     * @param tagQueryRequest
     * @return {@link Page }<{@link Tag }>
     */
    @Override
    public Page<Tag> getListPage(TagQueryRequest tagQueryRequest) {
        long current = tagQueryRequest.getCurrent();
        long size = tagQueryRequest.getPageSize();
        if (size == 0L) {
            size = PageConstant.PAGE_SIZE;
        }
        if (current == 0L) {
            current = PageConstant.CURRENT_PAGE;
        }
        // 查询数据库
        return this.page(new Page<>(current, size), this.getQueryWrapper(tagQueryRequest));
    }

    /**
     * 分页获取标签封装
     *
     * @param tagPage
     * @param request
     * @return
     */
    @Override
    public Page<TagVO> getTagVOPage(Page<Tag> tagPage, HttpServletRequest request) {
        List<Tag> tagList = tagPage.getRecords();
        Page<TagVO> tagVOPage = new Page<>(tagPage.getCurrent(), tagPage.getSize(), tagPage.getTotal());
        if (CollUtil.isEmpty(tagList)) {
            return tagVOPage;
        }
        // 对象列表 => 封装对象列表
        List<TagVO> tagVOList = tagList.stream().map(tag -> {
            return TagVO.objToVo(tag);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = tagList.stream().map(Tag::getCreateBy).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        //Map<Long, Boolean> tagIdHasPraiseMap = new HashMap<>();
        //Map<Long, Boolean> tagIdHasCollectMap = new HashMap<>();
        //User loginUser = userService.getLoginUserPermitNull(request);
        //if (loginUser != null) {
        //    Set<Long> tagIdSet = tagList.stream().map(Tag::getId).collect(Collectors.toSet());
        //    loginUser = userService.getLoginUser(request);
        //    // 获取点赞
        //    LambdaQueryWrapper<TagPraise> tagPraiseQueryWrapper = new LambdaQueryWrapper<>();
        //    tagPraiseQueryWrapper.in(TagPraise::getId, tagIdSet);
        //    tagPraiseQueryWrapper.eq(TagPraise::getCreateBy, loginUser.getId());
        //    List<TagPraise> tagTagPraiseList = tagThumbMapper.selectList(tagThumbQueryWrapper);
        //    tagTagThumbList.forEach(tagTagPraise -> tagIdHasPraiseMap.put(tagTagPraise.getTagId(), true));
        //    // 获取收藏
        //    LambdaQueryWrapper<TagCollect> tagCollectQueryWrapper = new LambdaQueryWrapper<>();
        //    tagCollectQueryWrapper.in(TagCollect::getId, tagIdSet);
        //    tagCollectQueryWrapper.eq(TagCollect::getCreateBy, loginUser.getId());
        //    List<TagCollect> tagCollectList = tagCollectMapper.selectList(tagCollectQueryWrapper);
        //    tagCollectList.forEach(tagCollect -> tagIdHasCollectMap.put(tagCollect.getTagId(), true));
        //}
        // 填充信息
        tagVOList.forEach(tagVO -> {
            Long userId = tagVO.getCreateBy();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            tagVO.setUser(userService.getUserVO(user));
        });
        // endregion

        tagVOPage.setRecords(tagVOList);
        return tagVOPage;
    }

    /**
     * 更新标签
     *
     * @param tagUpdateRequest 更新后请求
     * @param request          请求
     * @return long
     */
    @Override
    public long updateTag(TagUpdateRequest tagUpdateRequest, HttpServletRequest request) {
        // 本人和管理员可修改
        User user = userService.getLoginUser(request);
        if (!UserConstant.ADMIN_ROLE.equals(user.getUserRole()) && !Objects.equals(user.getId(), tagUpdateRequest.getCreateBy())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        long id = tagUpdateRequest.getId();
        // 获取数据
        Tag oldTag = tagMapper.selectById(id);
        ThrowUtils.throwIf(oldTag == null, ErrorCode.NOT_FOUND_ERROR);
        String tagName = tagUpdateRequest.getTagName();
        String category = tagUpdateRequest.getCategory();
        oldTag.setTagName(tagName);
        oldTag.setCategory(category);
        // 参数校验
        validTag(oldTag, false);
        // 更新
        tagMapper.updateById(oldTag);
        return id;
    }

    /**
     * 处理分页和验证
     *
     * @param tagQueryRequest 标签查询请求
     * @param request         请求
     * @return {@code Page<TagVO>}
     */
    @Override
    public Page<TagVO> handlePaginationAndValidation(TagQueryRequest tagQueryRequest, HttpServletRequest request) {
        long current = tagQueryRequest.getCurrent();
        long size = tagQueryRequest.getPageSize();
        if (size == 0L) {
            size = PageConstant.PAGE_SIZE;
        }
        if (current == 0L) {
            current = PageConstant.CURRENT_PAGE;
        }
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Tag> tagPage = this.page(new Page<>(current, size), this.getQueryWrapper(tagQueryRequest));
        return this.getTagVOPage(tagPage, request);
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
        Tag oldTag = tagMapper.selectById(id);
        ThrowUtils.throwIf(oldTag == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可操作（这里假设"编辑"和"删除"操作的权限是一样的）
        if (!oldTag.getCreateBy().equals(loginUser.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "你当前暂无该权限！");
        }
    }

    @Override
    public List<String> findExistingTags(List<String> tagsJson) {
        return tagMapper.findExistingTags(tagsJson);
    }

}
