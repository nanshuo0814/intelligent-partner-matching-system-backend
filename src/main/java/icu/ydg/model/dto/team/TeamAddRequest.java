package icu.ydg.model.dto.team;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 创建队伍请求
 *
 * @author 袁德光
 * @Date 2024/11/03
 */
@Data
@ApiModel(value = "TeamAddRequest", description = "创建队伍请求")
public class TeamAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;
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
    @ApiModelProperty(required = true, example = "1997-01-01 00:00:00", value = "过期日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
}