package icu.ydg.model.dto.userTeam;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户踢请求
 *
 * @author 袁德光
 * @date 2024/12/01
 */
@Data
public class UserKickRequest {
    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id")
    private Long userId;
    /**
     * 团队id
     */
    @ApiModelProperty(value = "团队id")
    private Long teamId;
}
