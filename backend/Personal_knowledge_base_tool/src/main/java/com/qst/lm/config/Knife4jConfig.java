package com.qst.lm.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: zhonglufa
 * @CreateTime: 2025-10-10
 * @Description: Knife4j配置类，用于整合Swagger生成API文档
 * @Version: 2.0
 */
@Configuration
public class Knife4jConfig {

    /**
     * 创建OpenAPI对象，配置API文档基本信息
     * @return OpenAPI
     */
    @Bean
    public OpenAPI openAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("个人知识库系统API接口文档")
                        .description("个人知识库系统API接口文档，提供收藏、知识、标签等相关接口")
                        .version("1.0")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("developer@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")));
    }

    /**
     * 用户认证模块API分组
     * @return GroupedOpenApi
     */
    @Bean
    public GroupedOpenApi authAPI(){
        return GroupedOpenApi.builder()
                .group("用户认证模块")
                .pathsToMatch("/user/**")
                .build();
    }

    /**
     * 收藏集模块API分组
     * @return GroupedOpenApi
     */
    @Bean
    public GroupedOpenApi collectionManagementAPI(){
        return GroupedOpenApi.builder()
                .group("收藏集管理模块")
                .pathsToMatch("/collections/**")
                .build();
    }

    /**
     * 收藏项模块API分组
     * @return GroupedOpenApi
     */
    @Bean
    public GroupedOpenApi collectionItemAPI(){
        return GroupedOpenApi.builder()
                .group("收藏项管理模块")
                .pathsToMatch("/collect/**")
                .build();
    }

    /**
     * 分类模块API分组
     * @return GroupedOpenApi
     */
    @Bean
    public GroupedOpenApi categoryAPI(){
        return GroupedOpenApi.builder()
                .group("分类管理模块")
                .pathsToMatch("/categories/**")
                .build();
    }

    /**
     * 标签模块API分组
     * @return GroupedOpenApi
     */
    @Bean
    public GroupedOpenApi tagAPI(){
        return GroupedOpenApi.builder()
                .group("标签管理模块")
                .pathsToMatch("/tags/**")
                .build();
    }

    /**
     * 笔记模块API分组
     * @return GroupedOpenApi
     */
    @Bean
    public GroupedOpenApi noteAPI(){
        return GroupedOpenApi.builder()
                .group("笔记管理模块")
                .pathsToMatch("/note/**")
                .build();
    }

    /**
     * 互动模块API分组
     * @return GroupedOpenApi
     */
    @Bean
    public GroupedOpenApi interactionAPI(){
        return GroupedOpenApi.builder()
                .group("互动模块")
                .pathsToMatch("/interaction/**")
                .build();
    }

    /**
     * 搜索模块API分组
     * @return GroupedOpenApi
     */
    @Bean
    public GroupedOpenApi searchAPI(){
        return GroupedOpenApi.builder()
                .group("搜索模块")
                .pathsToMatch("/search/**")
                .build();
    }

    /**
     * 通知模块API分组
     * @return GroupedOpenApi
     */
    @Bean
    public GroupedOpenApi notificationAPI(){
        return GroupedOpenApi.builder()
                .group("通知管理模块")
                .pathsToMatch("/notification/**")
                .build();
    }

    /**
     * 后台管理模块API分组
     * @return GroupedOpenApi
     */
    @Bean
    public GroupedOpenApi adminAPI(){
        return GroupedOpenApi.builder()
                .group("后台管理模块")
                .pathsToMatch("/admin/**")
                .build();
    }

}