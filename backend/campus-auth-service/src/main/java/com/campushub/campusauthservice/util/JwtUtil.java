package com.campushub.campusauthservice.util;

import com.campushub.common.constant.JwtConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 *
 * 负责 AccessToken 和 RefreshToken 的生成、解析、验证。
 * 使用 HMAC-SHA256 算法签名，密钥从配置文件读取。
 *
 * @author CampusHub
 */
@Component
public class JwtUtil {

    /**
     * JWT 签名密钥（从 application.yml 读取）
     */
    @Value("${campushub.jwt.secret}")
    private String secret;

    /**
     * AccessToken 有效期（毫秒），默认 2 小时
     */
    @Value("${campushub.jwt.access-token-expiration:7200000}")
    private long accessTokenExpiration;

    /**
     * RefreshToken 有效期（毫秒），默认 7 天
     */
    @Value("${campushub.jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpiration;

    /**
     * 获取签名密钥
     *
     * @return SecretKey 对象
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 AccessToken
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @param role     用户角色
     * @return AccessToken 字符串
     */
    public String generateAccessToken(Long userId, String username, String role) {
        return generateToken(userId, username, role, JwtConstants.TOKEN_TYPE_ACCESS, accessTokenExpiration);
    }

    /**
     * 生成 RefreshToken
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @param role     用户角色
     * @return RefreshToken 字符串
     */
    public String generateRefreshToken(Long userId, String username, String role) {
        return generateToken(userId, username, role, JwtConstants.TOKEN_TYPE_REFRESH, refreshTokenExpiration);
    }

    /**
     * 生成 Token 通用方法
     *
     * @param userId     用户 ID
     * @param username   用户名
     * @param role       用户角色
     * @param tokenType  Token 类型（access / refresh）
     * @param expiration 有效期（毫秒）
     * @return JWT 字符串
     */
    private String generateToken(Long userId, String username, String role,
                                 String tokenType, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtConstants.CLAIM_USER_ID, userId);
        claims.put(JwtConstants.CLAIM_USERNAME, username);
        claims.put(JwtConstants.CLAIM_ROLE, role);
        claims.put(JwtConstants.CLAIM_TOKEN_TYPE, tokenType);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析 Token，获取 Claims
     *
     * @param token JWT 字符串
     * @return Claims 对象
     * @throws io.jsonwebtoken.JwtException Token 无效或已过期
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Token 中获取用户 ID
     *
     * @param token JWT 字符串
     * @return 用户 ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get(JwtConstants.CLAIM_USER_ID, Long.class);
    }

    /**
     * 从 Token 中获取用户名
     *
     * @param token JWT 字符串
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get(JwtConstants.CLAIM_USERNAME, String.class);
    }

    /**
     * 从 Token 中获取用户角色
     *
     * @param token JWT 字符串
     * @return 用户角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get(JwtConstants.CLAIM_ROLE, String.class);
    }

    /**
     * 从 Token 中获取 Token 类型
     *
     * @param token JWT 字符串
     * @return Token 类型（access / refresh）
     */
    public String getTokenTypeFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get(JwtConstants.CLAIM_TOKEN_TYPE, String.class);
    }

    /**
     * 验证 Token 是否有效（未过期且签名正确）
     *
     * @param token JWT 字符串
     * @return true=有效，false=无效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取 Token 剩余有效时间（毫秒）
     *
     * @param token JWT 字符串
     * @return 剩余毫秒数；token 无效或已过期时返回 0
     */
    public long getRemainingValidity(String token) {
        try {
            Claims claims = parseToken(token);
            long remaining = claims.getExpiration().getTime() - System.currentTimeMillis();
            return Math.max(remaining, 0);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 判断 Token 是否为 AccessToken
     *
     * @param token JWT 字符串
     * @return true=是 AccessToken
     */
    public boolean isAccessToken(String token) {
        return JwtConstants.TOKEN_TYPE_ACCESS.equals(getTokenTypeFromToken(token));
    }

    /**
     * 判断 Token 是否为 RefreshToken
     *
     * @param token JWT 字符串
     * @return true=是 RefreshToken
     */
    public boolean isRefreshToken(String token) {
        return JwtConstants.TOKEN_TYPE_REFRESH.equals(getTokenTypeFromToken(token));
    }

    /**
     * 获取 AccessToken 有效期（毫秒）
     *
     * @return 有效期
     */
    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    /**
     * 获取 RefreshToken 有效期（毫秒）
     *
     * @return 有效期
     */
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}
