package icu.ydg.model.enums.file;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件上传业务类型枚举
 *
 * @author 袁德光
 * @date 2024/01/26 14:23:52
 */
@Getter
public enum FileUploadTypeEnums {

    // todo 业务类型枚举
    USER_AVATAR("用户头像", "user_avatar"),
    HOME_PAGE_COVER("首页封面", "home_page_cover"),
    TEAM_COVER("队伍封面", "team_cover"),
    POST_COVER("帖子封面", "post_cover");

    private final String text;

    private final String value;

    FileUploadTypeEnums(String text, String value) {
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
     * @param value 值
     * @return {@code FileUploadTypeEnums}
     */
    public static FileUploadTypeEnums getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (FileUploadTypeEnums anEnum : FileUploadTypeEnums.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

}
