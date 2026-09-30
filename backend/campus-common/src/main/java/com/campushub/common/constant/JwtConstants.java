package com.campushub.common.constant;

/**
 * JWT 相关常量
 */
public final class JwtConstants {

    private JwtConstants() {
    }

    /**
     * JWT 中的用户 ID
     */
    public static final String CLAIM_USER_ID = "userId";

    /**
     * JWT 中的用户名
     */
    public static final String CLAIM_USERNAME = "username";

    /**
     * JWT 中的用户角色
     */
    public static final String CLAIM_ROLE = "role";

    /**
     * JWT 中的 Token 类型
     */
    public static final String CLAIM_TOKEN_TYPE = "tokenType";

    /**
     * Access Token
     */
    public static final String TOKEN_TYPE_ACCESS = "access";

    /**
     * Refresh Token
     */
    public static final String TOKEN_TYPE_REFRESH = "refresh";
}