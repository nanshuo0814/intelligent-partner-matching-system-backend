package icu.ydg.model.dto.friend;

import icu.ydg.model.dto.page.PageBaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询好友请求
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "FriendQueryRequest", description = "创建好友请求")
public class FriendQueryRequest extends PageBaseRequest implements Serializable {

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
     * id
     */
    @ApiModelProperty(value = "不包含的id")
    private Long notId;

    /**
     * 搜索词
     */
    @ApiModelProperty(value = "搜索词")
    private String searchText;

    /**
     * 创建用户 id
     */
    @ApiModelProperty(value = "创建用户 id")
    private Long createBy;

    private static final long serialVersionUID = 1L;
}