package icu.ydg.model.dto.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户密码更新Request
 *
 * @author 袁德光
 * @date 2024/01/04 22:50:31
 */
@Data
@ApiModel(value = "UserPasswordUpdateRequest", description = "用户密码更新信息请求DTO")
public class UserPasswordUpdateRequest implements Serializable {

    private static final long serialVersionUID = 8383202174723157092L;

    @ApiModelProperty(value = "原密码", required = true)
    private String oldPassword;

    @ApiModelProperty(value = "新密码", required = true)
    private String newPassword;

    @ApiModelProperty(value = "确认密码", required = true)
    private String checkPassword;

}
