package icu.ydg.model.dto.postComment;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建帖子评论请求
 *
 * @author 袁德光
 * @Date 2024/11/02
 */
@Data
@ApiModel(value = "PostCommentAddRequest", description = "创建帖子评论请求")
public class PostCommentAddRequest implements Serializable {

    // todo 更多参数属性可自行添加

    /**
     * 内容
     */
    @ApiModelProperty(value = "内容")
    private String content;
    /**
     * 被评论的用户id
     */
    @ApiModelProperty(value = "被评论的用户id")
    private Long toUserId;

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

    private static final long serialVersionUID = 1L;
}