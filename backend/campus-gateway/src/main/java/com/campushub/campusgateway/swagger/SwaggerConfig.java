package com.campushub.campusgateway.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger 配置类（基于 SpringDoc OpenAPI 3.0）
 * <p>
 * 各微服务的文档聚合通过 {@code springdoc.swagger-ui.urls} 静态配置实现（见 application.yml），
 * 每个服务的文档端点挂在各自网关路由前缀下（如认证服务 {@code /api/auth/v3/api-docs}），
 * 无需额外的 /swagger-resources 端点（那是 Springfox 的约定，SpringDoc 的 UI 不会调用）。
 * </p>
 *
 * @author CampusHub Team
 * @since 1.0.0
 */
@Configuration
public class SwaggerConfig {

    /**
     * 网关自身 OpenAPI 基本信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CampusHub API 文档")
                        .description("校园综合服务平台统一 API 文档入口")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("CampusHub Team")
                                .email("admin@campushub.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
