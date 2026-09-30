package com.campushub.campususerservice.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库表：campus_user
 *
 * @author CampusHub
 */
@Data
@TableName("campus_user")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID，数据库自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录用户名，唯一，不允许修改 */
    private String username;

    /** BCrypt 加密密码，绝不返回前端 */
    @TableField("password_hash")
    private String passwordHash;

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
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间，数据库自动维护 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}