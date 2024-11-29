package icu.ydg.model.dto.post;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 帖子更新请求
 *
 * @author 袁德光
 * @date 2024/07/26
 */
@Data
@ApiModel(value = "PostUpdateRequest", description = "帖子更新请求DTO")
public class PostUpdateRequest implements Serializable {

    /**
     * id
     */
    @ApiModelProperty(value = "帖子状态（0：审核中，1：审核通过，2：审核未通过）")
    private Integer status;

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    private Long id;

    /**
     * 标题
     */
    @ApiModelProperty(value = "标题")
    private String title;

    /**
     * 内容
     */
    @ApiModelProperty(value = "内容")
    private String content;

    /**
     * 创建者
     */
    @ApiModelProperty(value = "创建者")
    private Long createBy;

    /**
     * 封面图片
     */
    @ApiModelProperty(value = "帖子封面图片")
    private String coverImage;

    private static final long serialVersionUID = 1L;
}