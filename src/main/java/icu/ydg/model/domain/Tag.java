package icu.ydg.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 标签表
 * @TableName tag
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="tag")
@Data
public class Tag extends CommonBaseEntity implements Serializable {

    /**
     * 标签名称
     */
    @TableField(value = "tag_name")
    @ApiModelProperty(value = "标签名称")
    private String tagName;

    /**
     * 分类
     */
    @TableField(value = "category")
    @ApiModelProperty(value = "分类")
    private String category;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}