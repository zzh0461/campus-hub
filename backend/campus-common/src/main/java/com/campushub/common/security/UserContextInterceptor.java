package com.campushub.common.security;

import com.campushub.common.constant.HeaderConstants;
import com.campushub.common.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户上下文拦截器
 * <p>
 * 从网关透传的 {@code X-User-Id} / {@code X-User-Role} 请求头中解析当前用户,
 * 填充到 {@link UserContext},并在请求结束后清理,防止线程池复用导致串号。
 * </p>
 * <p>
 * 注意:这两个请求头由网关在 JWT 校验通过后注入(网关会先删除客户端伪造的同名头),
 * 服务不应信任未经网关的直连请求中的该请求头。
 * </p>
 */
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        String userId = request.getHeader(HeaderConstants.USER_ID);
        if (StringUtils.hasText(userId)) {
            try {
                UserContext.setUserId(Long.parseLong(userId));
            } catch (NumberFormatException ignored) {
                // 非法请求头,忽略,保持上下文为空
            }
        }
        String role = request.getHeader(HeaderConstants.USER_ROLE);
        if (StringUtils.hasText(role)) {
            UserContext.setRole(role);
        }
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) {
        UserContext.clear();
    }
}
