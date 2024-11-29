package icu.ydg.model.vo.user;

import icu.ydg.model.domain.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户视图（脱敏）
 *
 * @author 袁德光
 * @date 2024/01/13 20:09:31
 */
@Data
@ApiModel(value = "UserVO", description = "用户安全视图VO")
public class UserVO implements Serializable {

    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id", required = true)
    private Long id;

    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户年龄", required = true)
    private Integer userAge;

    /**
     * 用户性别
     */
    @ApiModelProperty(value = "用户性别", required = true)
    private Integer userGender;

    /**
     * 用户昵称
     */
    @ApiModelProperty(value = "用户昵称", required = true)
    private String userName;

    /**
     * 用户账号
     */
    @ApiModelProperty(value = "用户账号", required = true)
    private String userAccount;

    /**
     * 用户标签
     */
    @ApiModelProperty(value = "用户标签", required = true)
    private String tags;

    /**
     * 用户头像
     */
    @ApiModelProperty(value = "用户头像", required = true)
    private String userAvatar;

    /**
     * 用户简介
     */
    @ApiModelProperty(value = "用户简介", required = true)
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    @ApiModelProperty(value = "用户角色", required = true)
    private String userRole;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

    /**
     * 创建人id
     */
    @ApiModelProperty(value = "创建人id", required = true)
    private Long createBy;

    /**
     * 将vo变为obj
     *
     * @param userVO 用户vo
     * @return {@link User }
     */
    public static User voToObj(UserVO userVO) {
        if (userVO == null) {
            return null;
        }
        User user = new User();
        BeanUtils.copyProperties(userVO, user);
        //List<String> tagList = postCommentVO.getTagList();
        //postComment.setTags(JSONUtil.toJsonStr(tagList));
        return user;
    }

    /**
     * 将obj存储为vo
     *
     * @param user 用户
     * @return {@link UserVO }
     */
    public static UserVO objToVo(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        //postCommentVO.setTagList(JSONUtil.toList(postComment.getTags(), String.class));
        return userVO;
    }

    private static final long serialVersionUID = 1L;

}