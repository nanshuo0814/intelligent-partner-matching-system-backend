package icu.ydg.model.dto.message;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建消息请求
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@Data
@ApiModel(value = "MessageAddRequest", description = "创建消息请求")
public class MessageAddRequest implements Serializable {

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