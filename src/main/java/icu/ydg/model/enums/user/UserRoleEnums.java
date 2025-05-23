package icu.ydg.model.enums.user;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户角色枚举
 *
 * @author 袁德光
 * @date 2023/12/30 20:18:27
 */
@Getter
public enum UserRoleEnums {

    /**
     * 普通用户
     */
    USER("普通用户", "user"),

    /**
     * 超级管理员
     */
    ADMIN("超级管理员", "admin"),

    /**
     * 账号已被封号
     */
    BAN("已封号", "ban");

    private final String text;

    private final String value;

    UserRoleEnums(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return {@code List<String>}
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 按值获取枚举
     * 根据 value 获取枚举
     *
     * @param value 价值
     * @return {@code UserRoleEnum}
     */
    public static UserRoleEnums getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (UserRoleEnums anEnum : UserRoleEnums.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

}
