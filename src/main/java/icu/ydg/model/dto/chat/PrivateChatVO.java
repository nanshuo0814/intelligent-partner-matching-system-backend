package icu.ydg.model.dto.chat;

import icu.ydg.model.vo.user.UserVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 私人聊天vo
 *
 * @author 南烁
 * @date 2024/11/28
 */
@Data
@ApiModel(value = "PrivateChatVO", description = "私聊返回")
public class PrivateChatVO implements Serializable, Comparable<PrivateChatVO> {

    private static final long serialVersionUID = -3426382762617526337L;

    /**
     * 用户
     */
    @ApiModelProperty(value = "用户")
    private UserVO user;

    /**
     * 最后一条消息
     */
    @ApiModelProperty(value = "最后消息")
    private String lastMessage;

    /**
     * 最后一条消息日期
     */
    @ApiModelProperty(value = "最后消息日期")
    private Date lastMessageDate;

    /**
     * 未读消息数量
     */
    @ApiModelProperty(value = "未读消息数量")
    private Integer unReadNum;

    @Override
    public int compareTo(PrivateChatVO other) {
        return -this.getLastMessageDate().compareTo(other.getLastMessageDate());
    }
}