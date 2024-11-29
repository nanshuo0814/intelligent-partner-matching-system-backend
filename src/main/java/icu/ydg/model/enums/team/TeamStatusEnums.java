package icu.ydg.model.enums.team;

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
public enum TeamStatusEnums {

    PUBLIC("公开", 0),
    PRIVATE("私有", 1),
    ENCRYPTION("加密", 2);

    private final String text;

    private final Integer value;

    TeamStatusEnums(String text, Integer value) {
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
    public static TeamStatusEnums getEnumByValue(Integer value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (TeamStatusEnums anEnum : TeamStatusEnums.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

}
