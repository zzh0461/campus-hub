package com.campushub.campusadminservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * CampusHub 管理服务启动类
 *
 * admin 是"聚合门面"(BFF)：自己不连数据库、不持有业务数据，
 * 全靠 OpenFeign 调用 user/content/market/activity 各服务的 internal 管理接口，
 * 把结果聚合后按前端后台契约返回。因此这里没有 @MapperScan（无持久层）。
 *
 * @author CampusHub
 */
@SpringBootApplication
@EnableFeignClients
public class CampusAdminServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusAdminServiceApplication.class, args);
    }

}
