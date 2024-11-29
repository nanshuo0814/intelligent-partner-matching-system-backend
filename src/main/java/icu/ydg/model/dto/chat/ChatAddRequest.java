package icu.ydg.model.dto.chat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建聊天请求
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@Data
@ApiModel(value = "ChatAddRequest", description = "创建聊天请求")
public class ChatAddRequest implements Serializable {

    // todo 更多参数属性可自行添加

    /**
     * 内容
     */
    @ApiModelProperty(value = "内容")
    private String content;

    /**
    * 标签列表
    */
    //@ApiModelProperty(value = "标签列表")
    //private List<String> tags;

    private static final long serialVersionUID = 1L;
}