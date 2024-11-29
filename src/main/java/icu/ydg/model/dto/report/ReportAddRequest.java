package icu.ydg.model.dto.report;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建反馈和举报请求
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@Data
@ApiModel(value = "ReportAddRequest", description = "创建反馈和举报请求")
public class ReportAddRequest implements Serializable {

    /**
     * 内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 被举报对象id
     */
    @TableField(value = "report_id")
    private Long reportId;

    /**
     * 类型（0：用户，1：帖子文章，2：帖子评论，3：聊天）
     */
    @TableField(value = "type")
    private Integer type;

    private static final long serialVersionUID = 1L;
}