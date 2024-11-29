package icu.ydg.model.dto.postComment;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新帖子评论请求
 *
 * @author 袁德光
 * @Date 2024/11/02
 */
@Data
@ApiModel(value = "PostCommentUpdateRequest", description = "创建帖子评论请求")
public class PostCommentUpdateRequest implements Serializable {

    // todo 更多参数属性可自行添加

    /**
     * 内容
     */
    @ApiModelProperty(value = "内容")
    private String content;

    /**
     * 帖子id
     */
    @ApiModelProperty(value = "帖子id")
    private Long postId;

    /**
     * 关联的1级评论id，如果是一级评论，则值为0
     */
    @ApiModelProperty(value = "关联的1级评论id，如果是一级评论，则值为0")
    private Long parentId;

    /**
     * 回复的评论id
     */
    @ApiModelProperty(value = "回复的评论id")
    private Long answerId;

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    private Long id;

    /**
     * 状态，0：正常，1：被举报，2：禁止查看
     */
    @ApiModelProperty(value = "状态，0：正常，1：被举报，2：禁止查看")
    private Integer status;

    private static final long serialVersionUID = 1L;
}