package com.campushub.campusgateway;

import com.campushub.campusgateway.config.GatewayProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(GatewayProperties.class)
public class CampusGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusGatewayApplication.class, args);
    }

}
