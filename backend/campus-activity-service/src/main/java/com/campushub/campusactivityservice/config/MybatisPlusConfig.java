package com.campushub.campusactivityservice.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置
 *
 * 注册分页插件：不注册的话 selectPage 分页会静默失效（查出全部数据）。
 * campus-common 未提供此配置，故在本服务内自行装配。
 *
 * @author CampusHub
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * MyBatis-Plus 拦截器：分页内部拦截器，指定数据库为 MySQL
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
