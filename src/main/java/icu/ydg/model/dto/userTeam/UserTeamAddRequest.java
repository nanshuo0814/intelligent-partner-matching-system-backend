package icu.ydg.model.dto.userTeam;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建用户队伍请求
 *
 * @author 袁德光
 * @Date 2024/11/03
 */
@Data
@ApiModel(value = "UserTeamAddRequest", description = "创建用户队伍请求")
public class UserTeamAddRequest implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 队伍id
     */
    @ApiModelProperty(example = "1", value = "队伍id")
    private Long teamId;

    /**
     * 密码
     */
    @ApiModelProperty(example = "123456", value = "密码")
    private String password;

}