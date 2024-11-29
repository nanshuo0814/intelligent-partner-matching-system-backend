package icu.ydg.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户队伍表
 *
 * @TableName user_team
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "user_team")
@Data
public class UserTeam extends CommonBaseEntity implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * 队伍id
     */
    @TableField(value = "team_id")
    @ApiModelProperty(example = "1", value = "队伍id")
    private Long teamId;

}