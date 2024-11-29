package icu.ydg.model.enums.sort;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import icu.ydg.model.domain.PostCommentPraise;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 帖子评论点赞排序字段枚举
 *
 * @author 袁德光
 * @Date 2024/11/03
 */
@Getter
@ApiModel(value = "PostCommentPraiseSortFieldEnums", description = "帖子评论点赞排序字段枚举")
public enum PostCommentPraiseSortFieldEnums {

    // todo 更多排序字段可自行添加

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    ID(PostCommentPraise::getId),
    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    CREATE_TIME(PostCommentPraise::getCreateTime),
    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    UPDATE_TIME(PostCommentPraise::getUpdateTime);

    private final SFunction<PostCommentPraise, ?> fieldGetter;

    PostCommentPraiseSortFieldEnums(SFunction<PostCommentPraise, ?> fieldGetter) {
        this.fieldGetter = fieldGetter;
    }

    private static final Map<String, PostCommentPraiseSortFieldEnums> FIELD_MAPPING = Arrays.stream(values())
            .collect(Collectors.toMap(PostCommentPraiseSortFieldEnums::name, field -> field));

    /**
     * 从字符串映射到枚举
     *
     * @param sortField 排序字段
     * @return {@code Optional<PostCommentPraiseSortField>}
     */
    public static Optional<PostCommentPraiseSortFieldEnums> fromString(String sortField) {
        // 转换驼峰式命名到下划线分隔，忽略大小写
        String formattedSortField = sortField.replaceAll("([a-z0-9])([A-Z])", "$1_$2");
        return Optional.ofNullable(FIELD_MAPPING.get(formattedSortField.toUpperCase()));
    }
}