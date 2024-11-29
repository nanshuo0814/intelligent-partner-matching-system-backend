package icu.ydg.model.dto.post;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 帖子添加请求
 *
 * @author 袁德光
 * @date 2024/07/26
 */
@Data
@ApiModel(value = "PostAddRequest", description = "帖子添加请求DTO")
public class PostAddRequest implements Serializable {

    /**
     * 标题
     */
    @ApiModelProperty(value = "帖子状态（0：审核中，1：审核通过，2：审核未通过）", required = false)
    private Integer status;

    /**
     * 标题
     */
    @ApiModelProperty(value = "标题", required = true)
    private String title;

    /**
     * 内容
     */
    @ApiModelProperty(value = "内容", required = true)
    private String content;


    /**
     * 封面图片
     */
    @ApiModelProperty(value = "帖子封面图片")
    private String coverImage;

    private static final long serialVersionUID = 1L;

}