package com.qst.lm.config;

import com.qst.lm.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvc配置类 - 注册拦截器
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebMvcConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns(
                        "/user/**",
                        "/collect/**",
                        "/collections/**",
                        "/categories/**",
                        "/category/**",
                        "/tags/**",
                        "/tag/**",
                        "/note/**",
                        "/notes/**",
                        "/search/**",
                        "/notification/**",
                        "/notifications/**",
                        "/interaction/**",
                        "/interactions/**",
                        "/admin/**"
                )
                .excludePathPatterns(
                        // 用户认证相关（无需登录）
                        "/user/login",
                        "/user/register",
                        "/user/send-verify-code",
                        "/user/reset-password",
                        // 公开内容接口（产品首页使用）
                        "/note/recommended",
                        "/collections/recommended",
                        "/note/public",
                        "/notes/public",
                        "/collections/public",
                        // 搜索热门词（无需登录）
                        "/search/hot",
                        // Knife4j文档（无需登录）
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-ui/**",
                        "/swagger-resources/**",
                        "/v3/api-docs",
                        "/v3/api-docs/**"
                );
    }
}
