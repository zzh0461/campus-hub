package com.campushub.common.constant;

/**
 * Redis Key 相关常量
 */
public final class RedisConstants {

    private RedisConstants() {
    }

    /**
     * Refresh Token
     *
     * campushub:login:refresh:{userId}
     */
    public static final String REFRESH_TOKEN_KEY_PREFIX =
            "campushub:login:refresh:";

    /**
     * Token 黑名单（登出或刷新轮换后失效的 Token）
     *
     * campushub:token:blacklist:{token}
     */
    public static final String TOKEN_BLACKLIST_KEY_PREFIX =
            "campushub:token:blacklist:";

    /**
     * 用户信息缓存
     *
     * campushub:user:info:{userId}
     */
    public static final String USER_INFO_KEY_PREFIX =
            "campushub:user:info:";

    /**
     * 验证码
     *
     * campushub:captcha:{target}
     */
    public static final String CAPTCHA_KEY_PREFIX =
            "campushub:captcha:";

    /**
     * 登录失败次数
     *
     * campushub:login:fail:{account}
     */
    public static final String LOGIN_FAIL_KEY_PREFIX =
            "campushub:login:fail:";

    /**
     * 分布式锁
     *
     * campushub:lock:{business}
     */
    public static final String LOCK_KEY_PREFIX =
            "campushub:lock:";
}