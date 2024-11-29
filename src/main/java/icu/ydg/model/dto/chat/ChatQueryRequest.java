package icu.ydg.model.dto.chat;

import icu.ydg.model.dto.page.PageBaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询聊天请求
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "ChatQueryRequest", description = "创建聊天请求")
public class ChatQueryRequest extends PageBaseRequest implements Serializable {

   // todo 更多参数属性可自行添加

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    private Long id;

    /**
     * id
     */
    @ApiModelProperty(value = "不包含的id")
    private Long notId;

    /**
     * 搜索词
     */
    @ApiModelProperty(value = "搜索词")
    private String searchText;

    /**
     * 标题
     */
    @ApiModelProperty(value = "标题")
    private String title;

    /**
     * 内容
     */
    @ApiModelProperty(value = "内容")
    private String content;

    /**
     * 创建用户 id
     */
    @ApiModelProperty(value = "创建用户 id")
    private Long createBy;

    /**
    * 标签列表
    */
    //@ApiModelProperty(value = "标签列表")
    //private List<String> tags;

    private static final long serialVersionUID = 1L;
}