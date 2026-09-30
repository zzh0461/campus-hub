package com.campushub.campusgateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关鉴权配置
 * @author CampusHub */
@Data
@ConfigurationProperties(prefix = "campushub.gateway")
public class GatewayProperties {

    /**
     * JWT 签名密钥：须与认证服务 campushub.jwt.secret 保持一致
     */
    private String jwtSecret;

    /**
     * 免鉴权白名单（Ant 路径模式），如 /api/auth/login、/api/auth/v3/api-docs
     */
    private List<String> whiteList = new ArrayList<>();
}
