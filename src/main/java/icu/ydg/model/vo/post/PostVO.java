package icu.ydg.model.vo.post;

import icu.ydg.model.domain.Post;
import icu.ydg.model.vo.user.UserVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 帖子视图
 *
 * @author 袁德光
 * @date 2024/07/26
 */
@Data
@ApiModel(value = "PostVO", description = "帖子视图VO")
public class PostVO implements Serializable {

    /**
     * id
     */
    @ApiModelProperty(value = "帖子id", required = true)
    private Long id;

    /**
     * 帖子状态（0：审核中，1：审核通过，2：审核未通过）
     */
    @ApiModelProperty(value = "帖子状态（0：审核中，1：审核通过，2：审核未通过）", required = true)
    private Integer status;

    /**
     * 标题
     */
    @ApiModelProperty(value = "帖子标题", required = true)
    private String title;

    /**
     * 封面图片
     */
    @ApiModelProperty(value = "帖子封面图片")
    private String coverImage;

    /**
     * 内容
     */
    @ApiModelProperty(value = "帖子内容", required = true)
    private String content;

    /**
     * 点赞数
     */
    @ApiModelProperty(value = "帖子点赞数", required = true)
    private Integer praiseNum;

    /**
     * 收藏数
     */
    @ApiModelProperty(value = "帖子收藏数", required = true)
    private Integer collectNum;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "帖子创建人id", required = true)
    private Long createBy;

    /**
     * 更新人
     */
    @ApiModelProperty(value = "帖子更新人id", required = true)
    private Long updateBy;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "帖子创建时间", required = true)
    private Date createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "帖子更新时间", required = true)
    private Date updateTime;

    /**
     * 创建人信息
     */
    @ApiModelProperty(value = "帖子创建人信息", required = true)
    private UserVO user;

    /**
     * 包装类转对象
     *
     * @param postVO post vo
     * @return {@code Post}
     */
    public static Post voToObj(PostVO postVO) {
        if (postVO == null) {
            return null;
        }
        Post post = new Post();
        BeanUtils.copyProperties(postVO, post);
        return post;
    }

    /**
     * 对象转包装类
     *
     * @param post post
     * @return {@code PostVO}
     */
    public static PostVO objToVo(Post post) {
        if (post == null) {
            return null;
        }
        PostVO postVO = new PostVO();
        BeanUtils.copyProperties(post, postVO);
        return postVO;
    }
}
