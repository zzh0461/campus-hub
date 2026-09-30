package com.campushub.campusactivityservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.campushub.campusactivityservice.mapper")
@EnableFeignClients
public class CampusActivityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusActivityServiceApplication.class, args);
    }

}
