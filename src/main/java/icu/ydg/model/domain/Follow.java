package icu.ydg.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 关注表
 * @TableName follow
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="follow")
@Data
public class Follow extends CommonBaseEntity implements Serializable {
    /**
     * 关注的用户id
     */
    @TableField(value = "follow_user_id")
    @ApiModelProperty(value = "关注的用户id")
    private Long followUserId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}