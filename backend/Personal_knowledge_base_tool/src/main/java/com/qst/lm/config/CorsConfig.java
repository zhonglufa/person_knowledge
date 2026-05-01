package com.qst.lm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置类 - 推荐写法
 * 使用 WebMvcConfigurer 方式，配置更直观，可读性更好
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 配置所有路径的跨域访问
        registry.addMapping("/**")
                // 允许来自指定域名的跨域请求（开发环境）
                .allowedOriginPatterns(
               "*"
                )
                // 允许携带认证信息（如 cookie、JWT 等）
                .allowCredentials(true)
                // 允许所有HTTP 方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                // 允许的请求头
                .allowedHeaders("*")
                // 预检请求缓存时间（秒）
                .maxAge(3600L);
    }
}