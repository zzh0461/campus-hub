package com.campushub.campusadminservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 管理后台 Web 配置
 *
 * 把 AdminAuthInterceptor 注册到 /api/admin/** 路径上。
 *
 * order(10) 是关键：common 的 UserContextInterceptor 注册在 /**、未显式设 order(默认 0)，
 * Spring MVC 按 order 升序执行拦截器，本拦截器 order 更大 → 排在其之后 →
 * 此时 UserContext 已填充好角色，权限判定才准确。
 *
 * @author CampusHub
 */
@Configuration
public class AdminWebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(new AdminAuthInterceptor())
                .addPathPatterns("/api/admin/**")
                .order(10);
    }
}
