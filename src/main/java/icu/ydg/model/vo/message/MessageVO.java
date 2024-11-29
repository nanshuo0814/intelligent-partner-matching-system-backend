package icu.ydg.model.vo.message;

import icu.ydg.model.domain.Message;
import icu.ydg.model.vo.user.UserVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 消息视图
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@Data
@ApiModel(value = "MessageAddRequest", description = "创建消息请求")
public class MessageVO implements Serializable {

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
     * @param messageVO
     * @return
     */
    public static Message voToObj(MessageVO messageVO) {
        if (messageVO == null) {
            return null;
        }
        Message message = new Message();
        BeanUtils.copyProperties(messageVO, message);
        //List<String> tagList = messageVO.getTagList();
        //message.setTags(JSONUtil.toJsonStr(tagList));
        return message;
    }

    /**
     * 对象转封装类
     *
     * @param message
     * @return
     */
    public static MessageVO objToVo(Message message) {
        if (message == null) {
            return null;
        }
        MessageVO messageVO = new MessageVO();
        BeanUtils.copyProperties(message, messageVO);
        //messageVO.setTagList(JSONUtil.toList(message.getTags(), String.class));
        return messageVO;
    }
}
