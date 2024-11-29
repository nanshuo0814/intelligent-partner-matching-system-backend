package icu.ydg.model.dto.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录Request
 *
 * @author 袁德光
 * @date 2024/01/04 18:58:48
 */
@Data
@ApiModel(value = "UserLoginRequest", description = "用户登录信息请求DTO")
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = -5262836669010105900L;

    /**
     * 账号
     */
    @ApiModelProperty(example = "ydg", value = "账号", required = true)
    private String userAccount;

    /**
     * 用户密码
     */
    @ApiModelProperty(example = "ydg.icu", value = "密码", required = true)
    private String userPassword;

}
