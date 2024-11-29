package icu.ydg.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 帖子评论表
 * @author 袁德光
 * @TableName post_comment
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="post_comment")
@Data
public class PostComment extends CommonBaseEntity implements Serializable {

    /**
     * 帖子id
     */
    @TableField(value = "post_id")
    @ApiModelProperty(value = "帖子id")
    private Long postId;
    /**
     * 被回复的用户id
     */
    @TableField(value = "to_user_id")
    @ApiModelProperty(value = "被回复的用户id")
    private Long toUserId;

    /**
     * 关联的1级评论id，如果是一级评论，则值为null
     */
    @TableField(value = "parent_id")
    @ApiModelProperty(value = "关联的1级评论id，如果是一级评论，则值为null")
    private Long parentId;

    /**
     * 回复的评论id
     */
    @TableField(value = "answer_id")
    @ApiModelProperty(value = "回复的评论id")
    private Long answerId;

    /**
     * 回复的内容
     */
    @TableField(value = "content")
    @ApiModelProperty(value = "回复的内容")
    private String content;

    /**
     * 点赞数
     */
    @TableField(value = "praise_num")
    @ApiModelProperty(value = "点赞数")
    private Integer praiseNum;

    /**
     * 状态，0：正常，1：被举报，2：禁止查看
     */
    @TableField(value = "status")
    @ApiModelProperty(value = "状态，0：正常，1：被举报，2：禁止查看")
    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}