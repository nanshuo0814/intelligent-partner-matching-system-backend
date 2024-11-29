package icu.ydg.model.enums.sort;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import icu.ydg.model.domain.Team;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 队伍排序字段枚举
 *
 * @author 袁德光
 * @Date 2024/11/03
 */
@Getter
@ApiModel(value = "TeamSortFieldEnums", description = "队伍排序字段枚举")
public enum TeamSortFieldEnums {

    // todo 更多排序字段可自行添加

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    ID(Team::getId),
    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    CREATE_TIME(Team::getCreateTime),
    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    UPDATE_TIME(Team::getUpdateTime);

    private final SFunction<Team, ?> fieldGetter;

    TeamSortFieldEnums(SFunction<Team, ?> fieldGetter) {
        this.fieldGetter = fieldGetter;
    }

    private static final Map<String, TeamSortFieldEnums> FIELD_MAPPING = Arrays.stream(values())
            .collect(Collectors.toMap(TeamSortFieldEnums::name, field -> field));

    /**
     * 从字符串映射到枚举
     *
     * @param sortField 排序字段
     * @return {@code Optional<TeamSortField>}
     */
    public static Optional<TeamSortFieldEnums> fromString(String sortField) {
        // 转换驼峰式命名到下划线分隔，忽略大小写
        String formattedSortField = sortField.replaceAll("([a-z0-9])([A-Z])", "$1_$2");
        return Optional.ofNullable(FIELD_MAPPING.get(formattedSortField.toUpperCase()));
    }
}