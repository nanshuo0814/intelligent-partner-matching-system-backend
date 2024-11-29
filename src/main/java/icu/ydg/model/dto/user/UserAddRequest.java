package icu.ydg.model.dto.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户添加Request
 *
 * @author 袁德光
 * @date 2024/01/06 12:00:19
 */
@Data
@ApiModel(value = "UserAddRequest", description = "用户添加信息请求DTO")
public class UserAddRequest implements Serializable {

    private static final long serialVersionUID = -119754408044041182L;

    /**
     * 用户昵称
     */
    @ApiModelProperty(value = "用户昵称")
    private String userName;

    /**
     * 账号
     */
    @ApiModelProperty(value = "账号")
    private String userAccount;

    /**
     * 用户性别
     */
    @ApiModelProperty(value = "用户性别")
    private Integer userGender;

    /**
     * 用户角色
     */
    @ApiModelProperty(value = "用户角色")
    private String userRole;
}
