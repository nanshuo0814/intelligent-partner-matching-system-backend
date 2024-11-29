package icu.ydg.model.enums.friend;

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
public enum FriendStatusEnums {

    /**
     * 不通过
     */
    NO_PASS("未通过", 0),
    PASS("已通过", 1),
    EXPIRE("已过期", 2),
    RESCINDED("已撤销", 3);

    private final String text;

    private final Integer value;

    FriendStatusEnums(String text, Integer value) {
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
    public static FriendStatusEnums getEnumByValue(Integer value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (FriendStatusEnums anEnum : FriendStatusEnums.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

}
