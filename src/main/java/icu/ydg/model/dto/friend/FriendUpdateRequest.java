package icu.ydg.model.dto.friend;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新好友请求
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@Data
@ApiModel(value = "FriendUpdateRequest", description = "创建好友请求")
public class FriendUpdateRequest implements Serializable {
    /**
     * 接收申请的用户id
     */
    @ApiModelProperty(value = "接收申请的用户id")
    private Long receiveId;

    /**
     * 是否已读(0-未读 1-已读)
     */
    @ApiModelProperty(value = "是否已读(0-未读 1-已读)")
    private Integer isRead;

    /**
     * 申请状态 默认0 （0-未通过 1-已同意 2-已过期 3-已撤销）
     */
    @ApiModelProperty(value = "申请状态 默认0 （0-未通过 1-已同意 2-已过期 3-已撤销）")
    private Integer status;

    /**
     * 好友申请备注信息
     */
    @ApiModelProperty(value = "好友申请备注信息")
    private String remark;
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    private Long id;

    /**
    * 创建者
    */
    @ApiModelProperty(value = "创建者")
    private Long createBy;

    private static final long serialVersionUID = 1L;
}