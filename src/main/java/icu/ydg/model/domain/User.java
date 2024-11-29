package icu.ydg.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户实体类
 *
 * @author 袁德光
 * @TableName user
 * @date 2024/01/04 14:43:30
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "user")
@ApiModel(value = "User", description = "用户实体类")
public class User extends CommonBaseEntity implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * 用户账号
     */
    @ApiModelProperty(value = "用户账号")
    @TableField(value = "user_account")
    private String userAccount;
    /**
     * 用户密码
     */
    @ApiModelProperty(value = "用户密码")
    @TableField(value = "user_password")
    private String userPassword;
    /**
     * 用户昵称
     */
    @ApiModelProperty(value = "用户昵称")
    @TableField(value = "user_name")
    private String userName;
    /**
     * 手机号
     */
    @TableField(value = "user_phone")
    @ApiModelProperty(value = "用户手机号")
    private String userPhone;

    /**
     * 0-女，1-男，2-未知
     */
    @ApiModelProperty(value = "0-女，1-男，2-未知")
    @TableField(value = "user_gender")
    private Integer userGender;
    /**
     * 邮箱
     */
    @ApiModelProperty(value = "邮箱")
    @TableField(value = "user_email")
    private String userEmail;
    /**
     * 用户头像
     */
    @ApiModelProperty(value = "用户头像")
    @TableField(value = "user_avatar")
    private String userAvatar;
    /**
     * 用户简介
     */
    @ApiModelProperty(value = "用户简介")
    @TableField(value = "user_profile")
    private String userProfile;
    /**
     * 用户角色：user/admin/ban
     */
    @ApiModelProperty(value = "用户角色：user/admin/ban")
    @TableField(value = "user_role")
    private String userRole;
    /**
     * 好友列表
     */
    @TableField(value = "friend_list")
    @ApiModelProperty(value = "好友列表")
    private String friendList;
    /**
     * 标签
     */
    @TableField(value = "tags")
    @ApiModelProperty(value = "标签")
    private String tags;
    /**
     * 年龄
     */
    @TableField(value = "user_age")
    @ApiModelProperty(value = "年龄")
    private Integer userAge;

}