package com.campushub.campususerservice.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户资料更新传输对象
 *
 * 字段与前端 types/user.ts 的 UpdateProfileParams 契约对齐，
 * 只包含允许用户自行修改的资料项，不含 username/role/status 等敏感或受控字段。
 */
@Data
public class UserDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 昵称 */
    @NotBlank(message = "昵称不能为空")
    private String nickname;

    /** 手机号 */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    /** 头像 URL */
    private String avatar;

    /** 个人简介 */
    @Size(max = 120, message = "简介不超过 120 字")
    private String bio;
}