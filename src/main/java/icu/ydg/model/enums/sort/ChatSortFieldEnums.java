package icu.ydg.model.enums.sort;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import icu.ydg.model.domain.Chat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 聊天排序字段枚举
 *
 * @author 袁德光
 * @Date 2024/11/04
 */
@Getter
@ApiModel(value = "ChatSortFieldEnums", description = "聊天排序字段枚举")
public enum ChatSortFieldEnums {

    // todo 更多排序字段可自行添加

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    ID(Chat::getId),
    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    CREATE_TIME(Chat::getCreateTime),
    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    UPDATE_TIME(Chat::getUpdateTime);

    private final SFunction<Chat, ?> fieldGetter;

    ChatSortFieldEnums(SFunction<Chat, ?> fieldGetter) {
        this.fieldGetter = fieldGetter;
    }

    private static final Map<String, ChatSortFieldEnums> FIELD_MAPPING = Arrays.stream(values())
            .collect(Collectors.toMap(ChatSortFieldEnums::name, field -> field));

    /**
     * 从字符串映射到枚举
     *
     * @param sortField 排序字段
     * @return {@code Optional<ChatSortField>}
     */
    public static Optional<ChatSortFieldEnums> fromString(String sortField) {
        // 转换驼峰式命名到下划线分隔，忽略大小写
        String formattedSortField = sortField.replaceAll("([a-z0-9])([A-Z])", "$1_$2");
        return Optional.ofNullable(FIELD_MAPPING.get(formattedSortField.toUpperCase()));
    }
}