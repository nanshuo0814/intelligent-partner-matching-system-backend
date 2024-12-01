package icu.ydg.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 消息表
 * @TableName message
 */
@TableName(value ="message")
@Data
public class Message implements Serializable {

    /**
     * id
     */
    @TableId(value = "id")
    @ApiModelProperty(value = "id")
    private Long id;
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
     * 发送消息id
     */
    @TableField(value = "create_by")
    private Long createBy;

    /**
     * 更新者id
     */
    @TableField(value = "update_by")
    private Long updateBy;

    /**
     * 队伍Id
     */
    @TableField(value = "team_id")
    @ApiModelProperty(value = "队伍id")
    private Long teamId;

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

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;
    /**
     * 逻辑删除，0:默认，1:删除
     */
    @TableField(value = "is_delete")
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}