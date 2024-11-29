package icu.ydg.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import icu.ydg.model.domain.User;
import icu.ydg.model.dto.user.UserGrowthRequest;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户 Mapper
 *
 * @author 袁德光
 * @date 2024/07/26
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 按标签查找用户
     *
     * @param tags 标签
     * @return {@link List }<{@link User }>
     */
    List<User> findUsersByTags(List<String> tags);

    /**
     * 获取随机用户
     *
     * @param num num
     * @return {@link List }<{@link User }>
     */
    List<User> getRandomUser(@Param("number") long num);

    List<UserGrowthRequest> getUserGrowthData();
}




