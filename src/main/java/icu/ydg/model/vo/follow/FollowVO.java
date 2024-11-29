package icu.ydg.model.vo.follow;

import icu.ydg.model.domain.Follow;
import icu.ydg.model.vo.user.UserVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 关注视图
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@Data
@ApiModel(value = "FollowVO", description = "关注VO请求")
public class FollowVO implements Serializable {

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    private Long id;

    /**
     * 创建用户 id
     */
    @ApiModelProperty(value = "创建用户 id")
    private Long createBy;

    /**
     * 被关注的用户id
     */
    @ApiModelProperty(value = "被关注的用户id")
    private Long followUserId;

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
     * 用户的信息
     */
    @ApiModelProperty(value = "用户的信息")
    private UserVO user;

    /**
     * 被关注用户的信息
     */
    @ApiModelProperty(value = "被关注用户的信息")
    private UserVO followUser;

    /**
     * 封装类转对象
     *
     * @param followVO
     * @return
     */
    public static Follow voToObj(FollowVO followVO) {
        if (followVO == null) {
            return null;
        }
        Follow follow = new Follow();
        BeanUtils.copyProperties(followVO, follow);
        //List<String> tagList = followVO.getTagList();
        //follow.setTags(JSONUtil.toJsonStr(tagList));
        return follow;
    }

    /**
     * 对象转封装类
     *
     * @param follow
     * @return
     */
    public static FollowVO objToVo(Follow follow) {
        if (follow == null) {
            return null;
        }
        FollowVO followVO = new FollowVO();
        BeanUtils.copyProperties(follow, followVO);
        //followVO.setTagList(JSONUtil.toList(follow.getTags(), String.class));
        return followVO;
    }
}
