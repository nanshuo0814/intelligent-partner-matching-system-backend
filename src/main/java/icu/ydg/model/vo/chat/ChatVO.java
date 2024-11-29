package icu.ydg.model.vo.chat;

import icu.ydg.model.domain.Chat;
import icu.ydg.model.vo.user.UserVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 聊天视图
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@Data
@ApiModel(value = "ChatAddRequest", description = "创建聊天请求")
public class ChatVO implements Serializable {

    // todo 更多参数属性可自行添加

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    private Long id;

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
    * 标签列表
    */
    //@ApiModelProperty(value = "标签列表")
    //private List<String> tags;

    /**
     * 封装类转对象
     *
     * @param chatVO
     * @return
     */
    public static Chat voToObj(ChatVO chatVO) {
        if (chatVO == null) {
            return null;
        }
        Chat chat = new Chat();
        BeanUtils.copyProperties(chatVO, chat);
        //List<String> tagList = chatVO.getTagList();
        //chat.setTags(JSONUtil.toJsonStr(tagList));
        return chat;
    }

    /**
     * 对象转封装类
     *
     * @param chat
     * @return
     */
    public static ChatVO objToVo(Chat chat) {
        if (chat == null) {
            return null;
        }
        ChatVO chatVO = new ChatVO();
        BeanUtils.copyProperties(chat, chatVO);
        //chatVO.setTagList(JSONUtil.toList(chat.getTags(), String.class));
        return chatVO;
    }
}
