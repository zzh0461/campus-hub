package com.campushub.campusauthservice.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录响应 VO
 *
 * @author CampusHub
 */
@Data
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * AccessToken
     */
    private String accessToken;

    /**
     * RefreshToken
     */
    private String refreshToken;

    /**
     * AccessToken 有效期（秒）
     */
    private Long expiresIn;

    /**
     * AccessToken 别名
     * <p>
     * 与前端契约（frontend/src/types/auth.ts 的 LoginResult）字段名对齐，
     * 值与 {@link #accessToken} 完全一致，前端从 result.token 取登录态。
     * </p>
     */
    private String token;

    /**
     * Token 类型（固定 Bearer）
     * <p>
     * 前端按 {@code Authorization: {tokenType} {token}} 组装请求头。
     * </p>
     */
    private String tokenType;

    /**
     * 用户信息
     */
    private UserVO user;
}
