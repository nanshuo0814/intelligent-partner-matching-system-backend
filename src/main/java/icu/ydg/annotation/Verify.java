package icu.ydg.annotation;

import java.lang.annotation.*;

/**
 * 验证：检查参数 + 检查权限
 * 该注解只能用在方法上指定开启某个或多个检查
 *
 * @author 袁德光
 * @date 2024/07/26
 */
@Target(ElementType.METHOD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Verify {

    /**
     * 检查身份验证(user,admin,ban)
     *
     * @return {@code String}
     */
    String checkAuth() default "";

}

