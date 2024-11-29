package icu.ydg.model.vo.friend;

import icu.ydg.model.domain.Friend;
import icu.ydg.model.vo.user.UserVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 好友视图
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@Data
@ApiModel(value = "FriendAddRequest", description = "创建好友请求")
public class FriendVO implements Serializable {
    /**
     * 接收申请的用户id
     */
    @ApiModelProperty(value = "接收申请的用户id ")
    private Long receiveId;

    /**
     * 是否已读(0-未读 1-已读)
     */
    @ApiModelProperty(value = "是否已读(0-未读 1-已读)")
    private Integer isRead;

    /**
     * 申请状态 默认0 （0-未通过 1-已同意 2-已过期 3-已撤销）
     */
    @ApiModelProperty(value = "申请状态 默认0 （0-未通过 1-已同意 2-已过期 3-已撤销）")
    private Integer status;

    /**
     * 好友申请备注信息
     */
    @ApiModelProperty(value = "好友申请备注信息")
    private String remark;
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
     * 被申请的用户信息
     */
    @ApiModelProperty(value = "被申请的用户信息")
    private UserVO user;

    public static Friend voToObj(FriendVO friendVO) {
        if (friendVO == null) {
            return null;
        }
        Friend friend = new Friend();
        BeanUtils.copyProperties(friendVO, friend);
        //List<String> tagList = friendVO.getTagList();
        //friend.setTags(JSONUtil.toJsonStr(tagList));
        return friend;
    }

    /**
     * 对象转封装类
     *
     * @param friend
     * @return
     */
    public static FriendVO objToVo(Friend friend) {
        if (friend == null) {
            return null;
        }
        FriendVO friendVO = new FriendVO();
        BeanUtils.copyProperties(friend, friendVO);
        //friendVO.setTagList(JSONUtil.toList(friend.getTags(), String.class));
        return friendVO;
    }
}
