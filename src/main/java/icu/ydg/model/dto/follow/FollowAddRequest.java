package icu.ydg.model.dto.follow;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建关注请求
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@Data
@ApiModel(value = "FollowAddRequest", description = "创建关注请求")
public class FollowAddRequest implements Serializable {

    /**
     * 关注的用户id
     */
    @ApiModelProperty(value = "关注的用户id")
    private Long followUserId;

    private static final long serialVersionUID = 1L;
}