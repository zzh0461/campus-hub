package com.campushub.campusauthservice.util;

import com.campushub.common.constant.JwtConstants;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JwtUtil 纯单元测试（不依赖 Spring 上下文与外部中间件）
 *
 * @author CampusHub
 */
class JwtUtilTest {

    private static final String SECRET = "CampusHub-JWT-Secret-Key-2024-For-Development-Only";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpiration", 7200000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshTokenExpiration", 604800000L);
    }

    @Test
    @DisplayName("AccessToken 生成后可解析出正确的 claims")
    void accessTokenRoundTrip() {
        String token = jwtUtil.generateAccessToken(1L, "student", "USER");

        Claims claims = jwtUtil.parseToken(token);
        assertEquals(1L, jwtUtil.getUserIdFromToken(token));
        assertEquals("student", jwtUtil.getUsernameFromToken(token));
        assertEquals("USER", jwtUtil.getRoleFromToken(token));
        assertEquals(JwtConstants.TOKEN_TYPE_ACCESS, jwtUtil.getTokenTypeFromToken(token));
        assertTrue(jwtUtil.isAccessToken(token));
        assertFalse(jwtUtil.isRefreshToken(token));
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    @DisplayName("RefreshToken 类型标记正确")
    void refreshTokenRoundTrip() {
        String token = jwtUtil.generateRefreshToken(2L, "campus_003", "USER");

        assertTrue(jwtUtil.isRefreshToken(token));
        assertFalse(jwtUtil.isAccessToken(token));
        assertEquals(2L, jwtUtil.getUserIdFromToken(token));
    }

    @Test
    @DisplayName("过期 Token 校验失败且剩余有效期为 0")
    void expiredTokenIsInvalid() {
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpiration", -1000L);
        String token = jwtUtil.generateAccessToken(1L, "student", "USER");

        assertFalse(jwtUtil.validateToken(token));
        assertEquals(0, jwtUtil.getRemainingValidity(token));
    }

    @Test
    @DisplayName("签名被篡改的 Token 校验失败")
    void tamperedTokenIsInvalid() {
        String token = jwtUtil.generateAccessToken(1L, "student", "USER");
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertFalse(jwtUtil.validateToken(tampered));
        assertEquals(0, jwtUtil.getRemainingValidity(tampered));
    }

    @Test
    @DisplayName("有效 Token 的剩余有效期大于 0")
    void remainingValidityIsPositive() {
        String token = jwtUtil.generateAccessToken(1L, "student", "USER");

        long remaining = jwtUtil.getRemainingValidity(token);
        assertTrue(remaining > 0 && remaining <= 7200000L);
    }

    @Test
    @DisplayName("不同用户/角色的 Token 彼此不同")
    void tokensDifferByContent() {
        String tokenA = jwtUtil.generateAccessToken(1L, "student", "USER");
        String tokenB = jwtUtil.generateAccessToken(2L, "admin", "ADMIN");

        assertNotEquals(tokenA, tokenB);
        assertEquals(2L, jwtUtil.getUserIdFromToken(tokenB));
    }

    @Test
    @DisplayName("完全非法的字符串解析返回无效")
    void garbageTokenIsInvalid() {
        assertFalse(jwtUtil.validateToken("not-a-jwt"));
        assertNull(safeGetUserId("not-a-jwt"));
    }

    /**
     * 非法 token 下 getUserIdFromToken 会抛异常，这里转为返回 null 辅助断言
     */
    private Long safeGetUserId(String token) {
        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }
}
