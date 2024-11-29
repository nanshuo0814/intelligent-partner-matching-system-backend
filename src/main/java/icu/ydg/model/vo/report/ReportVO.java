package icu.ydg.model.vo.report;

import icu.ydg.model.domain.Report;
import icu.ydg.model.vo.user.UserVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 反馈和举报视图
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@Data
@ApiModel(value = "ReportAddRequest", description = "创建反馈和举报请求")
public class ReportVO implements Serializable {
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
     * 内容
     */
    @ApiModelProperty(value = "内容")
    private String content;

    /**
     * 创建用户 id
     */
    @ApiModelProperty(value = "创建用户 id")
    private Long createBy;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    /**
     * 创建用户信息
     */
    @ApiModelProperty(value = "创建用户信息")
    private UserVO user;

    /**
     * 封装类转对象
     *
     * @param reportVO
     * @return
     */
    public static Report voToObj(ReportVO reportVO) {
        if (reportVO == null) {
            return null;
        }
        Report report = new Report();
        BeanUtils.copyProperties(reportVO, report);
        //List<String> tagList = reportVO.getTagList();
        //report.setTags(JSONUtil.toJsonStr(tagList));
        return report;
    }

    /**
     * 对象转封装类
     *
     * @param report
     * @return
     */
    public static ReportVO objToVo(Report report) {
        if (report == null) {
            return null;
        }
        ReportVO reportVO = new ReportVO();
        BeanUtils.copyProperties(report, reportVO);
        //reportVO.setTagList(JSONUtil.toList(report.getTags(), String.class));
        return reportVO;
    }
}
