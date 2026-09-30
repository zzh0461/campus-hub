package com.campushub.campusauthservice.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息 VO
 * 返回给前端的用户信息（不含密码）
 *
 * @author CampusHub
 */
@Data
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像 URL
     */
    private String avatar;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 角色: USER / ADMIN
     */
    private String role;

    /**
     * 状态: ACTIVE / DISABLED
     */
    private String status;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 注册时间
     */
    private LocalDateTime createdAt;
}
