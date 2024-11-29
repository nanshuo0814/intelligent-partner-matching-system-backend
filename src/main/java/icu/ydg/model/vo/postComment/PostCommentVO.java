package icu.ydg.model.vo.postComment;

import icu.ydg.model.domain.PostComment;
import icu.ydg.model.vo.user.UserVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 帖子评论视图
 *
 * @author 袁德光
 * @Date 2024/11/02
 */
@Data
@ApiModel(value = "PostCommentAddRequest", description = "创建帖子评论请求")
public class PostCommentVO implements Serializable {

    // 更多参数属性可自行添加


    /**
     * 帖子id
     */
    @ApiModelProperty(value = "帖子id")
    private Long postId;

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    private Long id;

    /**
     * 关联的1级评论id，如果是一级评论，则值为0
     */
    @ApiModelProperty(value = "关联的1级评论id，如果是一级评论，则值为0")
    private Long parentId;

    /**
     * 回复的评论id
     */
    @ApiModelProperty(value = "回复的评论id")
    private Long answerId;
    /**
     * 被回复的用户id
     */
    @ApiModelProperty(value = "被回复的用户id")
    private Long toUserId;

    /**
     * 回复的内容
     */
    @ApiModelProperty(value = "回复的内容")
    private String content;

    /**
     * 点赞数
     */
    @ApiModelProperty(value = "点赞数")
    private Integer praiseNum;

    /**
     * 状态，0：正常，1：被举报，2：禁止查看
     */
    @ApiModelProperty(value = "状态，0：正常，1：被举报，2：禁止查看")
    private Integer status;

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
     * 回复的用户信息
     */
    @ApiModelProperty(value = "回复的用户信息")
    private UserVO toUser;
    /**
     * 子评论
     */
    @ApiModelProperty(value = "子评论")
    private List<PostCommentVO> children;
    /**
     * 封装类转对象
     *
     * @param postCommentVO
     * @return
     */
    public static PostComment voToObj(PostCommentVO postCommentVO) {
        if (postCommentVO == null) {
            return null;
        }
        PostComment postComment = new PostComment();
        BeanUtils.copyProperties(postCommentVO, postComment);
        //List<String> tagList = postCommentVO.getTagList();
        //postComment.setTags(JSONUtil.toJsonStr(tagList));
        return postComment;
    }

    /**
     * 对象转封装类
     *
     * @param postComment
     * @return
     */
    public static PostCommentVO objToVo(PostComment postComment) {
        if (postComment == null) {
            return null;
        }
        PostCommentVO postCommentVO = new PostCommentVO();
        BeanUtils.copyProperties(postComment, postCommentVO);
        //postCommentVO.setTagList(JSONUtil.toList(postComment.getTags(), String.class));
        return postCommentVO;
    }
}
