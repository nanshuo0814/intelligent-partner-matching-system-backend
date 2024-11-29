package icu.ydg.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 队伍表
 * @TableName team
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="team")
@Data
public class Team extends CommonBaseEntity implements Serializable {

    /**
     * 名称
     */
    @TableField(value = "name")
    @ApiModelProperty(value = "队伍名称")
    private String name;

    /**
     * 描述
     */
    @TableField(value = "description")
    @ApiModelProperty(value = "队伍描述")
    private String description;

    /**
     * 封面图片
     */
    @TableField(value = "cover_image")
    @ApiModelProperty(value = "队伍封面图片")
    private String coverImage;

    /**
     * 最大人数
     */
    @TableField(value = "max_num")
    @ApiModelProperty(value = "队伍最大人数")
    private Integer maxNum;

    /**
     * 队伍当前的人数
     */
    @TableField(value = "current_num")
    @ApiModelProperty(value = "队伍当前的人数")
    private Integer currentNum;

    /**
     * 过期时间
     */
    @TableField(value = "expire_time")
    @ApiModelProperty(example = "2003-08-14 00:00:00", value = "过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    @TableField(value = "status")
    @ApiModelProperty(value = "队伍状态")
    private Integer status;

    /**
     * 密码
     */
    @TableField(value = "password")
    @ApiModelProperty(value = "队伍密码")
    private String password;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}