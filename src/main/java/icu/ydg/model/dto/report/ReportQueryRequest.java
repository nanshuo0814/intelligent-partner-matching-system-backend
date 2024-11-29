package icu.ydg.model.dto.report;

import icu.ydg.model.dto.page.PageBaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询反馈和举报请求
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "ReportQueryRequest", description = "创建反馈和举报请求")
public class ReportQueryRequest extends PageBaseRequest implements Serializable {
    /**
     * 被举报对象id
     */
    @ApiModelProperty(value = "被举报对象id")
    private Long reportId;

    /**
     * 类型（0：用户，1：帖子文章，2：帖子评论，3：聊天）
     */
    @ApiModelProperty(value = "类型（0：用户，1：帖子文章，2：帖子评论，3：聊天）")
    private Integer type;

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
     * id
     */
    @ApiModelProperty(value = "不包含的id")
    private Long notId;

    /**
     * 搜索词
     */
    @ApiModelProperty(value = "搜索词")
    private String searchText;

    /**
     * 内容
     */
    @ApiModelProperty(value = "内容")
    private String content;

    /**
     * 创建用户 id
     */
    @ApiModelProperty(value = "创建用户 id")
    private Long createBy;

    private static final long serialVersionUID = 1L;
}