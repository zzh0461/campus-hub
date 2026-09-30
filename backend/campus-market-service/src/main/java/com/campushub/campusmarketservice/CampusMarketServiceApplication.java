
package com.campushub.campusmarketservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients // 开启 Feign：扫描 @FeignClient 接口，自动生成"对讲机"代理
public class CampusMarketServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusMarketServiceApplication.class, args);
    }

}