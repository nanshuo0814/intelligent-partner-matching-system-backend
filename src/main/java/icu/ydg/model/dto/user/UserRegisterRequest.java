package icu.ydg.model.dto.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册 DTO
 *
 * @author 袁德光
 * @date 2023/12/23 19:00:34
 */
@Data
@ApiModel(value = "UserRegisterRequest", description = "用户注册信息请求DTO")
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = -3801105286374526414L;

    /**
     * 用户帐户
     */
    @ApiModelProperty(value = "账号", required = true)
    private String userAccount;

    /**
     * 用户密码
     */
    @ApiModelProperty(value = "密码", required = true)
    private String userPassword;

    /**
     * 检查密码
     */
    @ApiModelProperty(value = "确认密码", required = true)
    private String checkPassword;

}
