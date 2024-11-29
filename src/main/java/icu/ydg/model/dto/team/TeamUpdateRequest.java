package icu.ydg.model.dto.team;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 更新队伍请求
 *
 * @author 袁德光
 * @Date 2024/11/03
 */
@Data
@ApiModel(value = "TeamUpdateRequest", description = "创建队伍请求")
public class TeamUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    private Long id;
    /**
     * 名称
     */
    @ApiModelProperty(value = "名称")
    private String name;
    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    private String description;
    /**
     * 封面图片
     */
    @ApiModelProperty(value = "封面图片")
    private String coverImage;
    /**
     * 最大人数
     */
    @ApiModelProperty(value = "最大人数")
    private Integer maxNum;
    /**
     * 过期时间
     */
    @ApiModelProperty(value = "过期时间")
    private Date expireTime;
    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    @ApiModelProperty(value = "0 - 公开，1 - 私有，2 - 加密")
    private Integer status;
    /**
     * 密码
     */
    @ApiModelProperty(value = "密码")
    private String password;
    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    private Long createBy;
}