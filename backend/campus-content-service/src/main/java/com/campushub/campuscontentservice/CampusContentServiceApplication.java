package com.campushub.campuscontentservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients // 开启 Feign：扫描 @FeignClient 接口，失物招领填充发布者信息时调 user-service
@MapperScan("com.campushub.campuscontentservice.mapper") // 扫描 Mapper 接口，省去每个 Mapper 上标 @Mapper
public class CampusContentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusContentServiceApplication.class, args);
    }

}