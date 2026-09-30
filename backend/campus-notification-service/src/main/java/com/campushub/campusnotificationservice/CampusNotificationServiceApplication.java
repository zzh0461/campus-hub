package com.campushub.campusnotificationservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.campushub.campusnotificationservice.mapper")
public class CampusNotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusNotificationServiceApplication.class, args);
    }

}