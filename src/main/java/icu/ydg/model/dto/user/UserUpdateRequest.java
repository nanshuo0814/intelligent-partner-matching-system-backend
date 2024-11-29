package icu.ydg.model.dto.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新Request
 *
 * @author 袁德光
 * @date 2024/01/06 16:39:54
 */
@Data
@ApiModel(value = "UserUpdateRequest", description = "用户更新信息请求DTO")
public class UserUpdateRequest implements Serializable {

    private static final long serialVersionUID = -4905623571700412110L;

    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id")
    private Long id;
    /**
     * 用户年龄
     */
    @ApiModelProperty(value = "年龄")
    private Integer userAge;

    /**
     * 用户昵称
     */
    @ApiModelProperty(value = "用户昵称")
    private String userName;

    /**
     * 用户账号
     */
    @ApiModelProperty(value = "用户账号")
    private String userAccount;

    /**
     * 用户头像
     */
    @ApiModelProperty(value = "用户头像")
    private String userAvatar;

    /**
     * 用户邮箱
     */
    @ApiModelProperty(value = "用户邮箱")
    private String userEmail;

    /**
     * 用户性别
     */
    @ApiModelProperty(value = "用户性别")
    private Integer userGender;

    /**
     * 简介
     */
    @ApiModelProperty(value = "用户简介")
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    @ApiModelProperty(value = "用户角色")
    private String userRole;

    /**
     * 手机号
     */
    @ApiModelProperty(value = "用户手机号")
    private String userPhone;

    /**
     * 用户标签
     */
    @ApiModelProperty(value = "用户标签")
    private String tags;

}
