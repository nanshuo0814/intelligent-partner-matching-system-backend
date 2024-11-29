package icu.ydg.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 帖子评论点赞表
 * @TableName post_comment_praise
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="post_comment_praise")
@Data
public class PostCommentPraise extends CommonBaseEntity implements Serializable {

    /**
     * 帖子id
     */
    @TableField(value = "post_id")
    @ApiModelProperty(value = "帖子id")
    private Long postId;

    /**
     * 是否删除，0:默认，1:删除
     */
    @TableField(exist = false)
    private Integer isDelete;



    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}