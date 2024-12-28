package icu.ydg.model.dto.message;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 消息请求
 *
 * @author 袁德光
 * @date 2024/11/30
 */
@Data
@ApiModel(value = "MessageRequest", description = "信息请求")
public class MessageRequest implements Serializable {
    /**
     * 串行版本uid
     */
    private static final long serialVersionUID = 1324635911327892058L;
    /**
     * 为id
     */
    @ApiModelProperty(value = "接收id")
    private Long toId;
    /**
     * 团队id
     */
    @ApiModelProperty(value = "队伍id")
    private Long teamId;
    /**
     * 文本
     */
    @ApiModelProperty(value = "文本")
    private String text;

    /**
     * 文本消息内容类型（0：文字，1：图片，2：语音，3：其他）
     */
    @ApiModelProperty(value = "文本消息类型")
    private Integer textType;
    /**
     * 聊天类型
     */
    @ApiModelProperty(value = "聊天类型")
    private Integer chatType;
    /**
     * 是管理
     */
    @ApiModelProperty(value = "是否为管理员")
    private boolean isAdmin;
}