package com.campushub.campusauthservice.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * Token 响应 VO
 * 刷新接口返回的新 AccessToken / RefreshToken（RefreshToken 轮换后，客户端需覆盖保存旧值）
 *
 * @author CampusHub
 */
@Data
public class TokenVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 新的 AccessToken
     */
    private String accessToken;

    /**
     * 新的 RefreshToken（轮换机制下每次刷新都会更换）
     */
    private String refreshToken;

    /**
     * AccessToken 有效期（秒）
     */
    private Long expiresIn;
}
