package icu.ydg.model.dto.tag;

import icu.ydg.model.dto.page.PageBaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询标签请求
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "TagQueryRequest", description = "创建标签请求")
public class TagQueryRequest extends PageBaseRequest implements Serializable {

    /**
     * 标签名称
     */
    @ApiModelProperty(value = "标签名称")
    private String tagName;

    /**
     * 分类
     */
    @ApiModelProperty(value = "分类")
    private String category;
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
     * 创建用户 id
     */
    @ApiModelProperty(value = "创建用户 id")
    private Long createBy;

    private static final long serialVersionUID = 1L;
}