package icu.ydg.model.vo.homePageContentDisplay;

import icu.ydg.model.domain.HomePageContentDisplay;
import icu.ydg.model.vo.user.UserVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 首页内容展示视图
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@Data
@ApiModel(value = "HomePageContentDisplayAddRequest", description = "创建首页内容展示请求")
public class HomePageContentDisplayVO implements Serializable {

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    private Long id;

    /**
     * 内容
     */
    @ApiModelProperty(value = "内容")
    private String content;

    /**
     * 创建用户 id
     */
    @ApiModelProperty(value = "创建用户 id")
    private Long createBy;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    /**
     * 创建用户信息
     */
    @ApiModelProperty(value = "创建用户信息")
    private UserVO user;

    /**
    * 标签列表
    */
    //@ApiModelProperty(value = "标签列表")
    //private List<String> tags;

    /**
     * 封装类转对象
     *
     * @param homePageContentDisplayVO
     * @return
     */
    public static HomePageContentDisplay voToObj(HomePageContentDisplayVO homePageContentDisplayVO) {
        if (homePageContentDisplayVO == null) {
            return null;
        }
        HomePageContentDisplay homePageContentDisplay = new HomePageContentDisplay();
        BeanUtils.copyProperties(homePageContentDisplayVO, homePageContentDisplay);
        //List<String> tagList = homePageContentDisplayVO.getTagList();
        //homePageContentDisplay.setTags(JSONUtil.toJsonStr(tagList));
        return homePageContentDisplay;
    }

    /**
     * 对象转封装类
     *
     * @param homePageContentDisplay
     * @return
     */
    public static HomePageContentDisplayVO objToVo(HomePageContentDisplay homePageContentDisplay) {
        if (homePageContentDisplay == null) {
            return null;
        }
        HomePageContentDisplayVO homePageContentDisplayVO = new HomePageContentDisplayVO();
        BeanUtils.copyProperties(homePageContentDisplay, homePageContentDisplayVO);
        //homePageContentDisplayVO.setTagList(JSONUtil.toList(homePageContentDisplay.getTags(), String.class));
        return homePageContentDisplayVO;
    }
}
