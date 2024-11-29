package icu.ydg.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 帖子
 *
 * @author 袁德光
 * @date 2024/07/26
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "post")
@Data
public class Post extends CommonBaseEntity implements Serializable {

    /**
     * 帖子状态（0：审核中，1：审核通过，2：审核未通过）
     */
    @TableField(value = "status")
    @ApiModelProperty(value = "帖子状态（0：审核中，1：审核通过，2：审核未通过）")
    private Integer status;

    /**
     * 标题
     */
    @TableField(value = "title")
    @ApiModelProperty(value = "帖子标题")
    private String title;

    /**
     * 内容
     */
    @TableField(value = "content")
    @ApiModelProperty(value = "帖子内容")
    private String content;

    /**
     * 封面图片
     */
    @TableField(value = "cover_image")
    @ApiModelProperty(value = "帖子封面图片")
    private String coverImage;

    /**
     * 点赞数
     */
    @TableField(value = "praise_num")
    private Integer praiseNum;

    /**
     * 收藏数
     */
    @TableField(value = "collect_num")
    private Integer collectNum;

    /**
     * 评论数
     */
    @TableField(value = "comment_num")
    private Integer commentNum;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}