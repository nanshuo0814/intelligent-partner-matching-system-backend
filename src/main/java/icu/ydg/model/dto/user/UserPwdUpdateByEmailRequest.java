package icu.ydg.model.dto.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户密码重置Request
 *
 * @author 袁德光
 * @date 2024/01/04 21:24:00
 */
@Data
@ApiModel(value = "UserPwdUpdateByEmailRequest", description = "用户邮箱修改密码请求DTO")
public class UserPwdUpdateByEmailRequest implements Serializable {

    private static final long serialVersionUID = 7417360309354655142L;

    @ApiModelProperty(value = "用户账号", required = true)
    private String userAccount;

    @ApiModelProperty(value = "密码", required = true)
    private String userPassword;

    @ApiModelProperty(value = "确认密码", required = true)
    private String checkPassword;

    @ApiModelProperty(value = "邮箱", required = true)
    private String userEmail;

    @ApiModelProperty(value = "邮箱验证码", required = true)
    private String emailCaptcha;

}
