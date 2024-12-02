package icu.ydg.model.enums.message;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息类型枚举
 *
 * @author 袁德光
 * @date 2024/11/07
 */
@Getter
public enum MessageTypeEnums {
    //1：伙伴关注，2：帖子评论，3：私聊，4：队伍群聊，5：官方公共群聊
    FOLLOW("伙伴关注", 1),
    POST_PRAISE("帖子点赞", 2),
    PRIVATE_CHAT("聊天", 3),
    TEAM_CHAT("队伍群聊", 4),
    OFFICIAL_CHAT("官方公共群聊", 5),
    FRIEND_APPLY("好友申请", 6);

    private final String text;

    private final Integer value;

    MessageTypeEnums(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return {@code List<String>}
     */
    public static List<Integer> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 按值获取枚举
     * 根据 value 获取枚举
     *
     * @param value 价值
     * @return {@code UserGenderEnums}
     */
    public static MessageTypeEnums getEnumByValue(Integer value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (MessageTypeEnums anEnum : MessageTypeEnums.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

}
