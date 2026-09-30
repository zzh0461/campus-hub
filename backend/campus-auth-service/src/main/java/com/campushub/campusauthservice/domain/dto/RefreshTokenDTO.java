package com.campushub.campusauthservice.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 刷新 Token 请求 DTO
 *
 * @author CampusHub
 */
@Data
public class RefreshTokenDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * RefreshToken
     */
    @NotBlank(message = "RefreshToken 不能为空")
    private String refreshToken;
}
