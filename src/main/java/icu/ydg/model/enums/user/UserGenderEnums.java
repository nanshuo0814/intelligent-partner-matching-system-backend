package icu.ydg.model.enums.user;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户性别枚举
 *
 * @author 袁德光
 * @date 2023/12/30 20:18:27
 */
@Getter
public enum UserGenderEnums {

    FEMALE("女", 0),
    MALE("男", 1),
    NO("未知", 2);

    private final String text;

    private final Integer value;

    UserGenderEnums(String text, Integer value) {
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
    public static UserGenderEnums getEnumByValue(Integer value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (UserGenderEnums anEnum : UserGenderEnums.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

}
