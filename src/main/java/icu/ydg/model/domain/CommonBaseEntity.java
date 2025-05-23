package icu.ydg.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 公共基础实体
 *
 * @author 袁德光
 * @date 2024/07/26
 */
@Data
@ApiModel(value = "CommonBase", description = "公共实体类")
public class CommonBaseEntity implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 创建人id
     */
    @ApiModelProperty(value = "创建人id")
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 更新人id
     */
    @ApiModelProperty(value = "更新人id")
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    @ApiModelProperty(example = "2003-08-14 00:00:00", value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(example = "2003-08-14 00:00:00", value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 是否删除，0:默认，1:删除
     */
    @ApiModelProperty(value = "是否删除，0:默认，1:删除")
    @TableField(value = "is_delete")
    @TableLogic
    private Integer isDelete;

}
