package com.campushub.campusauthservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients // 开启Feign
public class CampusAuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusAuthServiceApplication.class, args);
    }

}
