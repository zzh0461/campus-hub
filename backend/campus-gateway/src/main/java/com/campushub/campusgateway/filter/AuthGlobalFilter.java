package com.campushub.campusgateway.filter;

import com.campushub.campusgateway.config.GatewayProperties;
import com.campushub.campusgateway.util.GatewayJwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 网关统一鉴权过滤器
 * <p>
 * 职责：
 * 1. 白名单路径（登录/注册/刷新/文档/健康检查等）直接放行；
 * 2. 其余路径校验 Authorization: Bearer Token（签名 + 有效期 + 必须是 AccessToken）；
 * 3. 校验 Token 是否在 Redis 黑名单（登出/轮换后的 Token 直接拒绝，Redis 异常时降级放行并告警）；
 * 4. 删除客户端伪造的 X-User-Id / X-User-Role 请求头，
 *    校验通过后注入真实的用户信息头，下游服务通过 UserContextInterceptor 读取。
 * </p>
 *
 * @author CampusHub
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final String TOKEN_BLACKLIST_KEY_PREFIX = "campushub:token:blacklist:";

    private final GatewayProperties gatewayProperties;
    private final GatewayJwtUtil jwtUtil;
    private final ReactiveStringRedisTemplate reactiveStringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 白名单或 CORS 预检请求：删除伪造头后放行
        if (isWhiteListed(path) || request.getMethod() == org.springframework.http.HttpMethod.OPTIONS) {
            return chain.filter(exchange.mutate().request(stripUserHeaders(request)).build());
        }

        // 1. 提取 Bearer Token
        String authorization = request.getHeaders().getFirst(GatewayJwtUtil.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(GatewayJwtUtil.BEARER_PREFIX)) {
            return unauthorized(exchange, "未登录或登录已过期");
        }
        String token = authorization.substring(GatewayJwtUtil.BEARER_PREFIX.length()).trim();

        // 2. 校验签名与有效期（不合法抛异常）
        Claims claims;
        try {
            claims = jwtUtil.parseClaims(token);
        } catch (Exception e) {
            log.warn("Token 校验失败: path={}, reason={}", path, e.getMessage());
            return unauthorized(exchange, "未登录或登录已过期");
        }

        // 3. 必须是 AccessToken（RefreshToken 不能访问业务接口）
        if (!jwtUtil.isAccessToken(claims)) {
            return unauthorized(exchange, "Token 类型错误");
        }

        // 4. 校验黑名单（登出/轮换后的 Token 立即失效），Redis 异常时降级放行
        return reactiveStringRedisTemplate.hasKey(TOKEN_BLACKLIST_KEY_PREFIX + token)
                .onErrorResume(e -> {
                    log.warn("Token 黑名单校验失败，降级放行: {}", e.getMessage());
                    return Mono.just(false);
                })
                .flatMap(blacklisted -> {
                    if (Boolean.TRUE.equals(blacklisted)) {
                        return unauthorized(exchange, "登录已失效，请重新登录");
                    }
                    return chain.filter(exchange.mutate().request(buildAuthenticatedRequest(request, claims)).build());
                });
    }

    @Override
    public int getOrder() {
        // 先于日志过滤器（-100）执行
        return -200;
    }

    /**
     * 判断路径是否在鉴权白名单内
     */
    private boolean isWhiteListed(String path) {
        List<String> whiteList = gatewayProperties.getWhiteList();
        if (whiteList == null) {
            return false;
        }
        return whiteList.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    /**
     * 删除客户端伪造的用户信息头
     */
    private ServerHttpRequest stripUserHeaders(ServerHttpRequest request) {
        return request.mutate()
                .headers(headers -> {
                    headers.remove(GatewayJwtUtil.USER_ID);
                    headers.remove(GatewayJwtUtil.USER_ROLE);
                })
                .build();
    }

    /**
     * 构建注入用户信息头的下游请求
     */
    private ServerHttpRequest buildAuthenticatedRequest(ServerHttpRequest request, Claims claims) {
        return request.mutate()
                .headers(headers -> {
                    headers.remove(GatewayJwtUtil.USER_ID);
                    headers.remove(GatewayJwtUtil.USER_ROLE);
                    headers.set(GatewayJwtUtil.USER_ID, String.valueOf(jwtUtil.getUserId(claims)));
                    headers.set(GatewayJwtUtil.USER_ROLE, jwtUtil.getRole(claims));
                })
                .build();
    }

    /**
     * 返回 401 统一响应体（与 R 结构一致，HTTP 状态同步为 401）
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        try {
            Map<String, Object> body = Map.of(
                    "code", 401,
                    "message", message,
                    "data", "");
            byte[] bytes = objectMapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            log.error("构建 401 响应失败", e);
            return response.setComplete();
        }
    }
}
