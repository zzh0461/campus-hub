package com.campushub.campusmarketservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 上传文件静态资源映射
 *
 * 把本地磁盘的上传目录映射为 market 的 /uploads/** 静态路径，
 * 供 <img> 直连访问（绕过网关鉴权，因为 img 标签不携带 JWT）。
 *
 * @author CampusHub
 */
@Slf4j
@Configuration
public class UploadResourceConfig implements WebMvcConfigurer {

    @Value("${campushub.upload.dir:./uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path dir = Paths.get(uploadDir).toAbsolutePath();
        try {
            // 启动时先建好目录：目录"存在"时 toUri() 才会带结尾斜杠，也保证后续可写
            Files.createDirectories(dir);
        } catch (IOException e) {
            log.warn("创建上传目录失败: {}", dir, e);
        }
        String location = dir.toUri().toString();
        // 双保险：静态资源位置必须以 / 结尾，否则 Spring 会把最后一段当文件名、拼错路径导致 404
        if (!location.endsWith("/")) {
            location += "/";
        }
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}