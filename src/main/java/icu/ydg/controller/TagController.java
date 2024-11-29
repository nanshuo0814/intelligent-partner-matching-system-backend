package icu.ydg.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import icu.ydg.annotation.Verify;
import icu.ydg.common.ApiResponse;
import icu.ydg.common.ApiResult;
import icu.ydg.common.ErrorCode;
import icu.ydg.constant.UserConstant;
import icu.ydg.exception.BusinessException;
import icu.ydg.model.domain.Tag;
import icu.ydg.model.domain.User;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.tag.TagAddRequest;
import icu.ydg.model.dto.tag.TagQueryRequest;
import icu.ydg.model.dto.tag.TagUpdateRequest;
import icu.ydg.model.vo.tag.TagVO;
import icu.ydg.service.TagService;
import icu.ydg.service.UserService;
import icu.ydg.utils.ThrowUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 标签接口
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@RestController
@RequestMapping("/tag")
@Slf4j
//@Api(tags = "标签接口")
public class TagController {

    @Resource
    private TagService tagService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建标签(管理员权限)
     *
     * @param tagAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "创建标签")
    @Verify(checkAuth = UserConstant.ADMIN_ROLE)
    public ApiResponse<Long> addTag(@RequestBody TagAddRequest tagAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(tagAddRequest == null, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(tagService.addTag(tagAddRequest, request));
    }

    /**
     * 删除标签
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @ApiOperation(value = "删除标签")
    @Verify(checkAuth = UserConstant.ADMIN_ROLE)
    public ApiResponse<Long> deleteTag(@RequestBody IdRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(tagService.deleteTag(deleteRequest, request), "删除成功！");
    }

    /**
     * 更新标签（仅管理员可用）
     *
     * @param tagUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @ApiOperation(value = "更新标签（需要 user 权限）")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<Long> updateTag(@RequestBody TagUpdateRequest tagUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(tagUpdateRequest == null || tagUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(tagService.updateTag(tagUpdateRequest, request), "更新成功！");
    }

    /**
     * 根据 id 获取标签（封装类）
     *
     * @param idRequest
     * @return
     */
    @GetMapping("/get/vo")
    @ApiOperation(value = "根据 id 获取标签（封装类）")
    public ApiResponse<TagVO> getTagVOById(IdRequest idRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(idRequest == null || idRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 获取封装类
        return ApiResult.success(tagService.getTagVO(idRequest, request));
    }

    /**
     * 分页获取标签列表（仅管理员可用）
     *
     * @param tagQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @ApiOperation(value = "分页获取标签列表（仅管理员可用）")
    @Verify(checkAuth = UserConstant.ADMIN_ROLE)
    public ApiResponse<Page<Tag>> listTagByPage(@RequestBody TagQueryRequest tagQueryRequest) {
        ThrowUtils.throwIf(tagQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(tagService.getListPage(tagQueryRequest));
    }

    /**
     * 分页获取标签列表（封装类）
     *
     * @param tagQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    @ApiOperation(value = "分页获取标签列表（封装类）")
    public ApiResponse<Page<TagVO>> listTagVOByPage(@RequestBody TagQueryRequest tagQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(tagQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ApiResult.success(tagService.handlePaginationAndValidation(tagQueryRequest, request));
    }

    /**
     * 分页获取当前登录用户创建的标签列表
     *
     * @param tagQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    @ApiOperation(value = "分页获取当前登录用户创建的标签列表")
    public ApiResponse<Page<TagVO>> listMyTagVOByPage(@RequestBody TagQueryRequest tagQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(tagQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        tagQueryRequest.setCreateBy(loginUser.getId());
        return ApiResult.success(tagService.handlePaginationAndValidation(tagQueryRequest, request));
    }

    // endregion

    /**
     * 获取所有分类
     *
     * @return
     */
    @GetMapping("/get/all/category")
    @ApiOperation(value = "获取所有分类")
    public ApiResponse<List<String>> getAllCategory(HttpServletRequest request) {
        return ApiResult.success(tagService.getAllCategories());
    }

    /**
     * 获取所有标签Map
     *
     * @return
     */
    @GetMapping("/get/map")
    @ApiOperation(value = "获取所有标签Map")
    public ApiResponse<Object> getTagMap() {
        List<Tag> tagList = tagService.list();
        if (tagList == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        // TagMap 缓存
        final Cache<String, Map<String, List<Tag>>> tagMapCache = Caffeine.newBuilder().build();
        // 整个 TagMap 缓存 key
        final String FULL_TAG_MAP_KEY = "full_tag_map_key";
        // 优先取缓存
        Map<String, List<Tag>> tagMap = tagMapCache.get(FULL_TAG_MAP_KEY, key -> tagList.stream().map(tag -> {
            // 精简
            Tag newTag = new Tag();
            newTag.setTagName(tag.getTagName());
            newTag.setCategory(tag.getCategory());
            // newTag.setPostNum(tag.getPostNum());
            return newTag;
            // 按类别分组
        }).collect(Collectors.groupingBy(Tag::getCategory)));
        return ApiResult.success(tagMap);
    }

}
