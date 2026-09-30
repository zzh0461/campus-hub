package com.campushub.common.constant;

/**
 * HTTP 请求头相关常量
 */
public final class HeaderConstants {

    private HeaderConstants() {
    }

    /**
     * Authorization 请求头
     */
    public static final String AUTHORIZATION = "Authorization";

    /**
     * Bearer Token 前缀
     */
    public static final String BEARER_PREFIX = "Bearer ";

    /**
     * 请求 ID 请求头
     */
    public static final String REQUEST_ID = "X-Request-Id";

    /**
     * 用户 ID 请求头
     *
     * 注意：
     * 该请求头只能由可信的 Gateway / 内部服务设置，
     * 不能直接信任来自公网客户端的同名 Header。
     */
    public static final String USER_ID = "X-User-Id";

    /**
     * 用户角色请求头
     */
    public static final String USER_ROLE = "X-User-Role";
}