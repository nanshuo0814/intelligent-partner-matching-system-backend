package icu.ydg.model.vo.userTeam;

import icu.ydg.model.domain.UserTeam;
import icu.ydg.model.vo.user.UserVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户队伍视图
 *
 * @author 袁德光
 * @Date 2024/11/03
 */
@Data
@ApiModel(value = "UserTeamAddRequest", description = "创建用户队伍请求")
public class UserTeamVO implements Serializable {

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    private Long id;

    /**
     * 队伍id
     */
    @ApiModelProperty(value = "队伍id")
    private String teamId;

    /**
     * 加入时间
     */
    @ApiModelProperty(value = "加入时间")
    private String joinTime;

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
     * @param userTeamVO
     * @return
     */
    public static UserTeam voToObj(UserTeamVO userTeamVO) {
        if (userTeamVO == null) {
            return null;
        }
        UserTeam userTeam = new UserTeam();
        BeanUtils.copyProperties(userTeamVO, userTeam);
        //List<String> tagList = userTeamVO.getTagList();
        //userTeam.setTags(JSONUtil.toJsonStr(tagList));
        return userTeam;
    }

    /**
     * 对象转封装类
     *
     * @param userTeam
     * @return
     */
    public static UserTeamVO objToVo(UserTeam userTeam) {
        if (userTeam == null) {
            return null;
        }
        UserTeamVO userTeamVO = new UserTeamVO();
        BeanUtils.copyProperties(userTeam, userTeamVO);
        //userTeamVO.setTagList(JSONUtil.toList(userTeam.getTags(), String.class));
        return userTeamVO;
    }
}
