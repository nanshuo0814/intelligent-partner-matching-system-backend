package icu.ydg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import icu.ydg.model.domain.HomePageContentDisplay;
import icu.ydg.model.dto.IdBatchRequest;
import icu.ydg.model.dto.IdRequest;
import icu.ydg.model.dto.homePageContentDisplay.HomePageContentDisplayAddRequest;
import icu.ydg.model.dto.homePageContentDisplay.HomePageContentDisplayQueryRequest;
import icu.ydg.model.dto.homePageContentDisplay.HomePageContentDisplayUpdateRequest;
import icu.ydg.model.vo.homePageContentDisplay.HomePageContentDisplayVO;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 首页内容展示服务
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
public interface HomePageContentDisplayService extends IService<HomePageContentDisplay> {

    /**
     * 执行批量删除首页内容展示
     * 
     * @param deleteRequest
     * @param request
     */
    void batchDeleteHomePageContentDisplay(IdBatchRequest deleteRequest, HttpServletRequest request);

    /**
     * 校验数据
     *
     * @param homePageContentDisplay
     * @param add 对创建的数据进行校验
     */
    void validHomePageContentDisplay(HomePageContentDisplay homePageContentDisplay, boolean add);

    /**
     * 获取查询条件
     *
     * @param homePageContentDisplayQueryRequest
     * @return
     */
    LambdaQueryWrapper<HomePageContentDisplay> getQueryWrapper(HomePageContentDisplayQueryRequest homePageContentDisplayQueryRequest);
    
    /**
     * 获取首页内容展示封装
     *
     * @param idRequest
     * @param request
     * @return
     */
    HomePageContentDisplayVO getHomePageContentDisplayVO(IdRequest idRequest, HttpServletRequest request);

    /**
     * 分页获取首页内容展示封装
     *
     * @param homePageContentDisplayPage
     * @param request
     * @return
     */
    Page<HomePageContentDisplayVO> getHomePageContentDisplayVOPage(Page<HomePageContentDisplay> homePageContentDisplayPage, HttpServletRequest request);

    /**
    * 更新首页内容展示
    *
    * @param homePageContentDisplayUpdateRequest 更新后请求
    * @param request           请求
    * @return long
    */
    long updateHomePageContentDisplay(HomePageContentDisplayUpdateRequest homePageContentDisplayUpdateRequest, HttpServletRequest request);

    /**
    * 添加首页内容展示
    *
    * @param homePageContentDisplayAddRequest 首页内容展示添加请求
    * @param request               请求
    * @return long
    */
    long addHomePageContentDisplay(HomePageContentDisplayAddRequest homePageContentDisplayAddRequest, HttpServletRequest request);

    /**
    * 删除首页内容展示
    *
    * @param deleteRequest 删除请求
    * @param request       请求
    * @return int
    */
    long deleteHomePageContentDisplay(IdRequest deleteRequest, HttpServletRequest request);

    /**
    * “获取列表” 页
    *
    * @param homePageContentDisplayQueryRequest
    * @return {@link Page }<{@link HomePageContentDisplay }>
    */
    Page<HomePageContentDisplay> getListPage(HomePageContentDisplayQueryRequest homePageContentDisplayQueryRequest);

    /**
    * 处理分页和验证
    *
    * @param homePageContentDisplayQueryRequest
    * @param request                 请求
    * @return {@link Page }<{@link HomePageContentDisplayVO }>
    */
    Page<HomePageContentDisplayVO> handlePaginationAndValidation(HomePageContentDisplayQueryRequest homePageContentDisplayQueryRequest, HttpServletRequest request);

    /**
    * 只有我或管理员可以做
    *
    * @param request 请求
    * @param id      id
    */
    void onlyMeOrAdministratorCanDo(HttpServletRequest request, Long id);

    /**
     * 主页页面内容显示
     *
     * @return {@link Map }<{@link String }, {@link Object }>
     */
    Map<String, Object> homePageContentDisplay();
}
