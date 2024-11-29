package icu.ydg.model.vo.team;

import icu.ydg.model.domain.Team;
import icu.ydg.model.vo.user.UserVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 队伍视图
 *
 * @author 袁德光
 * @Date 2024/11/03
 */
@Data
@ApiModel(value = "TeamAddRequest", description = "创建队伍请求")
public class TeamVO implements Serializable {



    /**
     * 用户列表
     */
    @ApiModelProperty(value = "队伍封面图片")
    private List<UserVO> userList;
    /**
     * 封面图片
     */
    @ApiModelProperty(value = "队伍封面图片")
    private String coverImage;
        /**
     * 过期时间
     */
    @ApiModelProperty(example = "2003-08-14 00:00:00", value = "过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;
    /**
     * 状态
     */
    @ApiModelProperty(value = "状态")
    private Integer status;

    /**
     * 最大人数
     */
    @ApiModelProperty(value = "队伍最大人数")
    private Integer maxNum;

    /**
     * 队伍当前的人数
     */
    @ApiModelProperty(value = "队伍当前的人数")
    private Integer currentNum;

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
     * @param teamVO
     * @return
     */
    public static Team voToObj(TeamVO teamVO) {
        if (teamVO == null) {
            return null;
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamVO, team);
        return team;
    }

    /**
     * 对象转封装类
     *
     * @param team
     * @return
     */
    public static TeamVO objToVo(Team team) {
        if (team == null) {
            return null;
        }
        TeamVO teamVO = new TeamVO();
        BeanUtils.copyProperties(team, teamVO);
        return teamVO;
    }
}
