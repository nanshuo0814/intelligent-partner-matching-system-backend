package icu.ydg.model.vo.ws;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * web套接字vo
 *
 * @author 南烁
 * @date 2024/11/30
 */
@Data
@ApiModel(value = "WebSocketVO", description = "websocket返回VO")
public class WebSocketVO implements Serializable {

    private static final long serialVersionUID = 4696612253320170315L;

    @ApiModelProperty(value = "id")
    private long id;

    /**
     * 用户昵称
     */
    @ApiModelProperty(value = "用户昵称")
    private String userName;

    /**
     * 账号
     */
    //@ApiModelProperty(value = "用户账号")
    //private String userAccount;

    /**
     * 用户头像
     */
    @ApiModelProperty(value = "用户头像")
    private String userAvatar;

}
