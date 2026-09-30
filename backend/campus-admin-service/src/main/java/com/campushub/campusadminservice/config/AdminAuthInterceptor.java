package com.campushub.campusadminservice.config;

import com.campushub.common.context.UserContext;
import com.campushub.common.enums.ResultCode;
import com.campushub.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理后台权限拦截器
 *
 * 守卫所有 /api/admin/** 接口：只有角色为 ADMIN 的用户才放行，否则抛 403(FORBIDDEN)。
 *
 * 为什么放在这里而不是每个 Controller 手写校验：
 * 后台每一个接口都要求管理员身份，用拦截器集中守一道门，避免每个方法重复写、也杜绝漏写。
 *
 * 角色从哪来：网关校验 JWT 通过后，会把 X-User-Role 透传下来，
 * common 的 UserContextInterceptor(注册在 /**、order 默认 0)先把它填进 UserContext；
 * 本拦截器在 AdminWebMvcConfig 里以更大的 order 注册，保证排在其之后执行，能安全读到角色。
 *
 * @author CampusHub
 */
public class AdminAuthInterceptor implements HandlerInterceptor {

    /** 管理员角色标识，与 campus_user 表 role 列、前端 authStore.isAdmin 判定保持一致 */
    private static final String ROLE_ADMIN = "ADMIN";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        // 未登录(角色为空)或普通用户，一律拒绝进入后台
        if (!ROLE_ADMIN.equals(UserContext.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return true;
    }
}
