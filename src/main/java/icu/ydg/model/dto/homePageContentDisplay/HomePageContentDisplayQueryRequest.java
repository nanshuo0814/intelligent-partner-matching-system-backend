package icu.ydg.model.dto.homePageContentDisplay;

import icu.ydg.model.dto.page.PageBaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询首页内容展示请求
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "HomePageContentDisplayQueryRequest", description = "创建首页内容展示请求")
public class HomePageContentDisplayQueryRequest extends PageBaseRequest implements Serializable {

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
     * id
     */
    @ApiModelProperty(value = "id")
    private Long id;

    /**
     * id
     */
    @ApiModelProperty(value = "不包含的id")
    private Long notId;

    /**
     * 搜索词
     */
    @ApiModelProperty(value = "搜索词")
    private String searchText;

    /**
     * 创建用户 id
     */
    @ApiModelProperty(value = "创建用户 id")
    private Long createBy;

    private static final long serialVersionUID = 1L;
}