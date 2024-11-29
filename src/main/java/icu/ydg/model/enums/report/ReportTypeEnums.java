package icu.ydg.model.enums.report;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 团队状态枚举
 *
 * @author 袁德光
 * @date 2024/11/03
 */
@Getter
public enum ReportTypeEnums {

    USER_REPORT("用户违规", 0),
    POST_REPORT("帖子内容违规", 1),
    POST_COMMENT_REPORT("帖子评论内容违规", 2),
    CHAT_REPORT("聊天内容违规", 3),
    TEAM_REPORT("队伍内容违规", 4);

    private final String text;

    private final Integer value;

    ReportTypeEnums(String text, Integer value) {
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
    public static ReportTypeEnums getEnumByValue(Integer value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (ReportTypeEnums anEnum : ReportTypeEnums.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

}
