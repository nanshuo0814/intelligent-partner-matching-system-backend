package icu.ydg.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局跨域配置
 *
 * @author 袁德光
 * @date 2024/01/29
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 覆盖所有请求
        registry.addMapping("/**")
                // 允许发送 Cookie
                .allowCredentials(true)
                // todo 放行哪些域名（必须用 patterns，否则 * 会和 allowCredentials 冲突）
                // 最好制定域名，不要写 *
                //.allowedOriginPatterns("*")
                .allowedOrigins(
                        "https://partner-admin.pages.dev",
                        "https://partner-user.pages.dev",
                        "http://42.194.148.139:5173", "http://localhost:5173", "http://42.194.148.139:9528", "http://localhost:9528"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}
