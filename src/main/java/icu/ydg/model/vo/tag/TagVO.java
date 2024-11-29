package icu.ydg.model.vo.tag;

import icu.ydg.model.domain.Tag;
import icu.ydg.model.vo.user.UserVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 标签视图
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@Data
@ApiModel(value = "TagAddRequest", description = "创建标签请求")
public class TagVO implements Serializable {
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
     * 创建用户信息
     */
    @ApiModelProperty(value = "创建用户信息")
    private UserVO user;

    /**
     * 封装类转对象
     *
     * @param tagVO
     * @return
     */
    public static Tag voToObj(TagVO tagVO) {
        if (tagVO == null) {
            return null;
        }
        Tag tag = new Tag();
        BeanUtils.copyProperties(tagVO, tag);
        //List<String> tagList = tagVO.getTagList();
        //tag.setTags(JSONUtil.toJsonStr(tagList));
        return tag;
    }

    /**
     * 对象转封装类
     *
     * @param tag
     * @return
     */
    public static TagVO objToVo(Tag tag) {
        if (tag == null) {
            return null;
        }
        TagVO tagVO = new TagVO();
        BeanUtils.copyProperties(tag, tagVO);
        //tagVO.setTagList(JSONUtil.toList(tag.getTags(), String.class));
        return tagVO;
    }
}
