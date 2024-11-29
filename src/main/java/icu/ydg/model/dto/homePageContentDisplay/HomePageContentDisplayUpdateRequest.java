package icu.ydg.model.dto.homePageContentDisplay;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新首页内容展示请求
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@Data
@ApiModel(value = "HomePageContentDisplayUpdateRequest", description = "创建首页内容展示请求")
public class HomePageContentDisplayUpdateRequest implements Serializable {

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
     * 0-通知栏 1-轮播图
     */
    @ApiModelProperty(value = "0-通知栏 1-轮播图")
    private Integer type;

    /**
    * 标签列表
    */
    //@ApiModelProperty(value = "标签列表")
    //private List<String> tags;

    private static final long serialVersionUID = 1L;
}