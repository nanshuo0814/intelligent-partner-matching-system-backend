package icu.ydg.model.dto.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户重置pwd请求
 *
 * @author 袁德光
 * @date 2024/06/25
 */
@Data
@ApiModel(value = "UserResetPwdRequest", description = "用户重置密码请求DTO")
public class UserResetPwdByEmailStepRequest implements Serializable {

    private static final long serialVersionUID = -8011153806807323196L;

    @ApiModelProperty(value = "邮箱", required = true)
    private String email;

    @ApiModelProperty(value = "新密码", required = true)
    private String newPassword;

    @ApiModelProperty(value = "确认密码", required = true)
    private String confirmPassword;

    @ApiModelProperty(value = "校验邮箱验证码成功的凭证", required = true)
    private String voucher;

}
