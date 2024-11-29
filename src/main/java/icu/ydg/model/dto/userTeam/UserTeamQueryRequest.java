package icu.ydg.model.dto.userTeam;

import icu.ydg.model.dto.page.PageBaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询用户队伍请求
 *
 * @author 袁德光
 * @Date 2024/11/03
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "UserTeamQueryRequest", description = "创建用户队伍请求")
public class UserTeamQueryRequest extends PageBaseRequest implements Serializable {

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
     * 队伍id
     */
    @ApiModelProperty(value = "队伍id")
    private Long teamId;

    /**
     * 加入时间
     */
    @ApiModelProperty(value = "加入时间")
    private String joinTime;

    /**
     * 标题
     */
    @ApiModelProperty(value = "标题")
    private String title;

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