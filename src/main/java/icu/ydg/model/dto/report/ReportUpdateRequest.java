package icu.ydg.model.dto.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新反馈和举报请求
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@Data
@ApiModel(value = "ReportUpdateRequest", description = "创建反馈和举报请求")
public class ReportUpdateRequest implements Serializable {

    /**
     * 内容
     */
    @ApiModelProperty(value = "内容")
    private String content;
    /**
     * 状态（0-未处理, 1-已处理）
     */
    @ApiModelProperty(value = "状态（0-未处理, 1-已处理）")
    private Integer status;

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    private Long id;

    /**
    * 创建者
    */
    @ApiModelProperty(value = "创建者")
    private Long createBy;

    private static final long serialVersionUID = 1L;
}