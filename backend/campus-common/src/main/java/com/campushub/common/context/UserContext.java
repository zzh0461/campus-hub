package com.campushub.common.context;

/**
 * 当前登录用户上下文
 *
 * 用于在一次 HTTP 请求生命周期内保存当前用户信息。
 */
public final class UserContext {

    private UserContext() {
    }

    private static final ThreadLocal<Long> USER_ID_HOLDER =
            new ThreadLocal<>();

    private static final ThreadLocal<String> USERNAME_HOLDER =
            new ThreadLocal<>();

    private static final ThreadLocal<String> ROLE_HOLDER =
            new ThreadLocal<>();

    /**
     * 设置当前用户 ID
     *
     * @param userId 用户 ID
     */
    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    /**
     * 获取当前用户 ID
     *
     * @return 用户 ID
     */
    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    /**
     * 设置当前用户名
     *
     * @param username 用户名
     */
    public static void setUsername(String username) {
        USERNAME_HOLDER.set(username);
    }

    /**
     * 获取当前用户名
     *
     * @return 用户名
     */
    public static String getUsername() {
        return USERNAME_HOLDER.get();
    }

    /**
     * 设置当前用户角色
     *
     * @param role 用户角色
     */
    public static void setRole(String role) {
        ROLE_HOLDER.set(role);
    }

    /**
     * 获取当前用户角色
     *
     * @return 用户角色
     */
    public static String getRole() {
        return ROLE_HOLDER.get();
    }

    /**
     * 清理当前请求的用户上下文
     *
     * 必须在请求结束后调用，
     * 防止线程池复用线程导致用户数据泄漏。
     */
    public static void clear() {
        USER_ID_HOLDER.remove();
        USERNAME_HOLDER.remove();
        ROLE_HOLDER.remove();
    }
}