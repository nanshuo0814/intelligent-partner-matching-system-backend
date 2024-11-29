package icu.ydg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import icu.ydg.model.domain.Tag;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.tag.TagAddRequest;
import icu.ydg.model.dto.tag.TagQueryRequest;
import icu.ydg.model.dto.tag.TagUpdateRequest;
import icu.ydg.model.vo.tag.TagVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 标签服务
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
public interface TagService extends IService<Tag> {

    /**
     * 校验数据
     *
     * @param tag
     * @param add 对创建的数据进行校验
     */
    void validTag(Tag tag, boolean add);

    /**
     * 获取查询条件
     *
     * @param tagQueryRequest
     * @return
     */
    LambdaQueryWrapper<Tag> getQueryWrapper(TagQueryRequest tagQueryRequest);
    
    /**
     * 获取标签封装
     *
     * @param idRequest
     * @param request
     * @return
     */
    TagVO getTagVO(IdRequest idRequest, HttpServletRequest request);

    /**
     * 分页获取标签封装
     *
     * @param tagPage
     * @param request
     * @return
     */
    Page<TagVO> getTagVOPage(Page<Tag> tagPage, HttpServletRequest request);

    /**
    * 更新标签
    *
    * @param tagUpdateRequest 更新后请求
    * @param request           请求
    * @return long
    */
    long updateTag(TagUpdateRequest tagUpdateRequest, HttpServletRequest request);

    /**
    * 添加标签
    *
    * @param tagAddRequest 标签添加请求
    * @param request               请求
    * @return long
    */
    long addTag(TagAddRequest tagAddRequest, HttpServletRequest request);

    /**
    * 删除标签
    *
    * @param deleteRequest 删除请求
    * @param request       请求
    * @return int
    */
    long deleteTag(IdRequest deleteRequest, HttpServletRequest request);

    /**
    * “获取列表” 页
    *
    * @param tagQueryRequest
    * @return {@link Page }<{@link Tag }>
    */
    Page<Tag> getListPage(TagQueryRequest tagQueryRequest);

    /**
    * 处理分页和验证
    *
    * @param tagQueryRequest
    * @param request                 请求
    * @return {@link Page }<{@link TagVO }>
    */
    Page<TagVO> handlePaginationAndValidation(TagQueryRequest tagQueryRequest, HttpServletRequest request);

    /**
    * 只有我或管理员可以做
    *
    * @param request 请求
    * @param id      id
    */
    void onlyMeOrAdministratorCanDo(HttpServletRequest request, Long id);

    /**
     * 查找现有标记
     *
     * @param tagsJson 标签json
     * @return {@link List }<{@link String }>
     */
    List<String> findExistingTags(List<String> tagsJson);

    List<String> getAllCategories();
}
