package icu.ydg.model.dto.tag;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新标签请求
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@Data
@ApiModel(value = "TagUpdateRequest", description = "创建标签请求")
public class TagUpdateRequest implements Serializable {
    /**
     * 标签名称
     */
    @ApiModelProperty(value = "标签名称")
    private String tagName;

    /**
     * 分类
     */
    @ApiModelProperty(value = "分类")
    private String category;
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    private Long id;

    /**
    * 创建者
    */
    @ApiModelProperty(value = "创建者")
    private Long createBy;

    private static final long serialVersionUID = 1L;
}