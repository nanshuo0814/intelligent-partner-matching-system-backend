package icu.ydg.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 首页内容展示表
 * @TableName home_page_content_display
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="home_page_content_display")
@Data
public class HomePageContentDisplay extends CommonBaseEntity implements Serializable {

    /**
     * 内容
     */
    @TableField(value = "content")
    @ApiModelProperty(value = "内容")
    private String content;

    /**
     * 0-通知栏 1-轮播图
     */
    @TableField(value = "type")
    @ApiModelProperty(value = "0-通知栏 1-轮播图")
    private Integer type;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}