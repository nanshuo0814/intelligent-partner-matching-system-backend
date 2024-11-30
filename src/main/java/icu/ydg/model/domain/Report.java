package icu.ydg.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 举报反馈表
 * @TableName report
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="report")
@Data
public class Report extends CommonBaseEntity implements Serializable {
    /**
     * 内容
     */
    @ApiModelProperty(value = "内容")
    @TableField(value = "content")
    private String content;

    /**
     * 被举报对象id
     */
    @ApiModelProperty(value = "被举报对象id")
    @TableField(value = "report_id")
    private Long reportId;

    /**
     * 类型（0：用户，1：帖子文章，2：帖子评论，3：聊天）
     */
    @ApiModelProperty(value = "类型（0：用户，1：帖子文章，2：帖子评论，3：聊天，4：队伍）")
    @TableField(value = "type")
    private Integer type;

    /**
     * 状态（0-未处理, 1-已处理）
     */
    @ApiModelProperty(value = "状态（0-未处理, 1-已处理）")
    @TableField(value = "status")
    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}