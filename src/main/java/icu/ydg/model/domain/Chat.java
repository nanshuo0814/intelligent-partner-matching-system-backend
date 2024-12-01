package icu.ydg.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 聊天表
 *
 * @TableName chat
 */
@TableName(value = "chat")
@Data
@ApiModel(value = "Chat", description = "聊天")
public class Chat implements Serializable {
    /**
     * id
     */
    @TableId(value = "id")
    @ApiModelProperty(value = "id")
    private Long id;

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
     * 接收消息id
     */
    @TableField(value = "to_id")
    private Long toId;

    /**
     * 消息内容
     */
    @TableField(value = "text")
    private String text;

    /**
     * 聊天类型 3-私聊 4-队伍群聊 5-大厅聊天
     */
    @TableField(value = "chat_type")
    private Integer chatType;

    /**
     * 是否已读 1-已读 2-未读
     */
    @TableField(value = "is_read")
    private Integer isRead;

    /**
     * 群聊id
     */
    @TableField(value = "team_id")
    private Long teamId;

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