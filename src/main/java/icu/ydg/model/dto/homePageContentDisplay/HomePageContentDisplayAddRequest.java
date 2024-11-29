package icu.ydg.model.dto.homePageContentDisplay;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建首页内容展示请求
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@Data
@ApiModel(value = "HomePageContentDisplayAddRequest", description = "创建首页内容展示请求")
public class HomePageContentDisplayAddRequest implements Serializable {

    /**
     * 内容
     */
    @ApiModelProperty(value = "内容")
    private String content;

    /**
     * 0-通知栏 1-轮播图
     */
    @TableField(value = "type")
    @ApiModelProperty(value = "0-通知栏 1-轮播图")
    private Integer type;

    private static final long serialVersionUID = 1L;
}