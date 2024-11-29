package icu.ydg.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 好友表
 * @TableName friend
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="friend")
@Data
public class Friend extends CommonBaseEntity implements Serializable {

    /**
     * 接收申请的用户id 
     */
    @TableField(value = "receive_id")
    private Long receiveId;

    /**
     * 是否已读(0-未读 1-已读)
     */
    @TableField(value = "is_read")
    private Integer isRead;

    /**
     * 申请状态 默认0 （0-未通过 1-已同意 2-已过期 3-已撤销）
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 好友申请备注信息
     */
    @TableField(value = "remark")
    private String remark;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}