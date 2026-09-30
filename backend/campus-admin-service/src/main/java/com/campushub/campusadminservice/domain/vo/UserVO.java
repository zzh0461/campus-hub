package com.campushub.campusadminservice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 后台用户信息 VO（admin 侧契约副本）
 *
 * 微服务间不共享业务 VO：admin 持有自己的一份，字段与 user-service 的 UserVO、
 * 前端 types/user.ts 的 User 契约对齐。Feign 收到 user-service 返回的 JSON 后，
 * 按字段名反序列化到本类，再套 R 返回给前端后台。
 *
 * @author CampusHub
 */
@Data
public class UserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 头像 URL */
    private String avatar;

    /** 手机号 */
    private String phone;

    /** 角色: USER / ADMIN */
    private String role;

    /** 状态: ACTIVE / DISABLED */
    private String status;

    /** 个人简介 */
    private String bio;

    /** 注册时间 */
    private LocalDateTime createdAt;
}
