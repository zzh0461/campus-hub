package com.campushub.common.config;

import com.campushub.common.exception.GlobalExceptionHandler;
import com.campushub.common.security.UserContextInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * common 模块 Web 自动装配
 * <p>
 * 通过 {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}
 * 注册,业务服务引入 campus-common 依赖即自动生效,无需扫描 com.campushub.common 包。
 * </p>
 * <p>
 * 仅在 Servlet(WebMVC)环境下生效;网关等 Reactor 应用不会装配。
 * 各 Bean 均支持 {@code @ConditionalOnMissingBean},业务侧可自行覆盖。
 * </p>
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class CommonWebAutoConfiguration {

    /**
     * 全局异常处理器:统一将异常转换为 R 响应结构
     */
    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    /**
     * 用户上下文拦截器:从网关注入的请求头中填充 UserContext
     */
    @Bean
    @ConditionalOnMissingBean
    public UserContextInterceptor userContextInterceptor() {
        return new UserContextInterceptor();
    }

    /**
     * 将用户上下文拦截器注册到 MVC 拦截器链
     */
    @Bean
    public WebMvcConfigurer userContextWebMvcConfigurer(UserContextInterceptor userContextInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(userContextInterceptor).addPathPatterns("/**");
            }
        };
    }
}
