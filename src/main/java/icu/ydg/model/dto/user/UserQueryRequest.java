package icu.ydg.model.dto.user;

import icu.ydg.model.dto.page.PageBaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 用户查询Request
 *
 * @author 袁德光
 * @date 2024/01/12 23:11:49
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "UserQueryRequest", description = "用户查询请求DTO")
public class UserQueryRequest extends PageBaseRequest implements Serializable {

    private static final long serialVersionUID = -7808183174434904160L;

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    private Long id;

    /**
     * 用户账号
     */
    @ApiModelProperty(value = "用户账号")
    private String userAccount;

    /**
     * 用户昵称
     */
    @ApiModelProperty(value = "用户昵称")
    private String userName;

    /**
     * 用户性别
     */
    @ApiModelProperty(value = "用户性别")
    private Integer userGender;

    /**
     * 用户邮箱
     */
    @ApiModelProperty(value = "用户邮箱")
    private String userEmail;

    /**
     * 手机号
     */
    @ApiModelProperty(value = "手机号")
    private String userPhone;

    /**
     * 简介
     */
    @ApiModelProperty(value = "用户简介")
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    @ApiModelProperty(value = "用户角色（user/admin/ban）")
    private String userRole;

    /**
     * 用户标签
     */
    @ApiModelProperty(value = "用户标签")
    private List<String> userTags;

}
