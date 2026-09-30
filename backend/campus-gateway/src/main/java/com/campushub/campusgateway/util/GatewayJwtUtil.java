package com.campushub.campusgateway.util;

import com.campushub.campusgateway.config.GatewayProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * 网关侧 JWT 解析工具
 * <p>
 * 仅做签名验证与 Claims 读取（不签发）。密钥须与认证服务 campushub.jwt.secret 一致。
 * 头部常量与 campus-common 的 HeaderConstants/JwtConstants 保持字面一致
 * （网关不依赖 campus-common，避免引入 Servlet 依赖破坏 WebFlux 环境）。
 * </p>
 *
 * @author CampusHub
 */
@Component
@RequiredArgsConstructor
public class GatewayJwtUtil {

    /** Authorization 请求头 */
    public static final String AUTHORIZATION = "Authorization";
    /** Bearer Token 前缀 */
    public static final String BEARER_PREFIX = "Bearer ";
    /** 下游用户 ID 请求头（由网关注入，客户端伪造的同名头会被删除） */
    public static final String USER_ID = "X-User-Id";
    /** 下游用户角色请求头 */
    public static final String USER_ROLE = "X-User-Role";
    /** JWT claim：Token 类型 */
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TOKEN_TYPE = "tokenType";
    private static final String TOKEN_TYPE_ACCESS = "access";

    private final GatewayProperties gatewayProperties;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(gatewayProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 解析并校验 Token（签名或过期不合法时抛 JJwtException / ExpiredJwtException）
     *
     * @param token JWT 字符串
     * @return Claims
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 是否为 AccessToken（拒绝用 RefreshToken 访问业务接口）
     */
    public boolean isAccessToken(Claims claims) {
        return TOKEN_TYPE_ACCESS.equals(claims.get(CLAIM_TOKEN_TYPE, String.class));
    }

    public Long getUserId(Claims claims) {
        return claims.get(CLAIM_USER_ID, Long.class);
    }

    public String getUsername(Claims claims) {
        return claims.get(CLAIM_USERNAME, String.class);
    }

    public String getRole(Claims claims) {
        return claims.get(CLAIM_ROLE, String.class);
    }
}
