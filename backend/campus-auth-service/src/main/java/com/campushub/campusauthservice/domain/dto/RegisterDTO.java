package com.campushub.campusauthservice.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 注册请求 DTO
 *
 * @author CampusHub
 */
@Data
public class RegisterDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度必须在 4-20 之间")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在 6-20 之间")
    private String password;

    /**
     * 昵称
     */
    @NotBlank(message = "昵称不能为空")
    @Size(max = 20, message = "昵称长度不能超过 20")
    private String nickname;

    /**
     * 手机号（可选，与前端契约一致）
     */
    @Size(max = 20, message = "手机号长度不能超过 20")
    private String phone;

    /**
     * 确认密码（可选，与前端契约一致，服务端做一致性兜底校验）
     */
    private String confirmPassword;
}
