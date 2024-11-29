package icu.ydg.model.dto.postComment;

import icu.ydg.model.dto.page.PageBaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询帖子评论请求
 *
 * @author 袁德光
 * @Date 2024/11/02
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "PostCommentQueryRequest", description = "创建帖子评论请求")
public class PostCommentQueryRequest extends PageBaseRequest implements Serializable {

    // todo 更多参数属性可自行添加

    /**
     * 标签列表
     */
    //@ApiModelProperty(value = "标签列表")
    //private List<String> tags;

    private static final long serialVersionUID = 1L;
    /**
     * 帖子id
     */
    @ApiModelProperty(value = "post_id")
    private Long postId;
    /**
     * 关联的1级评论id，如果是一级评论，则值为0
     */
    @ApiModelProperty(value = "parent_id")
    private Long parentId;
    /**
     * 回复的评论id
     */
    @ApiModelProperty(value = "answer_id")
    private Long answerId;
    /**
     * 被回复的用户ID
     */
    @ApiModelProperty(value = "to_user_id")
    private Long toUserId;
    /**
     * 回复的内容
     */
    @ApiModelProperty(value = "content")
    private String content;
    /**
     * 点赞数
     */
    @ApiModelProperty(value = "praise_num")
    private Integer praiseNum;
    /**
     * 状态，0：正常，1：被举报，2：禁止查看
     */
    @ApiModelProperty(value = "status")
    private Integer status;
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    private Long id;

    ///**
    // * 搜索词
    // */
    //@ApiModelProperty(value = "搜索词")
    //private String searchText;
    /**
     * id
     */
    @ApiModelProperty(value = "不包含的id")
    private Long notId;

    /**
     * 标题
     */
    //@ApiModelProperty(value = "标题")
    //private String title;
    /**
     * 创建用户 id
     */
    @ApiModelProperty(value = "创建用户 id")
    private Long createBy;
}