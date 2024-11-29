package icu.ydg.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 消息表
 * @TableName message
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="message")
@Data
public class Message extends CommonBaseEntity implements Serializable {
    /**
     * 1：伙伴关注，2：帖子评论，3：私聊，4：队伍群聊，5：官方公共群聊
     */
    @TableField(value = "type")
    @ApiModelProperty(value = "1：伙伴关注，2：帖子评论，3：私聊，4：队伍群聊，5：官方公共群聊")
    private Integer type;

    /**
     * 消息接收的用户id
     */
    @TableField(value = "to_id")
    @ApiModelProperty(value = "消息接收的用户id")
    private Long toId;

    /**
     * 消息内容
     */
    @TableField(value = "content")
    @ApiModelProperty(value = "消息内容")
    private String content;

    /**
     * 0 未读 ,1 已读
     */
    @TableField(value = "is_read")
    @ApiModelProperty(value = "0 未读 ,1 已读")
    private Integer isRead;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}