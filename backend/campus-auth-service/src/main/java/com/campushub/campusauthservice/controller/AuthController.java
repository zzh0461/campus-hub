package com.campushub.campusauthservice.controller;

import com.campushub.campusauthservice.domain.dto.LoginDTO;
import com.campushub.campusauthservice.domain.dto.RefreshTokenDTO;
import com.campushub.campusauthservice.domain.dto.RegisterDTO;
import com.campushub.campusauthservice.domain.vo.LoginVO;
import com.campushub.campusauthservice.domain.vo.TokenVO;
import com.campushub.campusauthservice.domain.vo.UserVO;
import com.campushub.campusauthservice.service.AuthService;
import com.campushub.common.constant.HeaderConstants;
import com.campushub.common.context.UserContext;
import com.campushub.common.enums.ResultCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * 处理用户登录、注册、Token 刷新、登出等认证相关请求。
 *
 * @author CampusHub
 */
@Tag(name = "认证管理", description = "用户登录、注册、Token 刷新、登出等接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     *
     * @param loginDTO 登录请求参数
     * @return 登录结果（AccessToken、RefreshToken、用户信息）
     */
    @Operation(summary = "用户登录", description = "验证用户名和密码，返回 AccessToken 和 RefreshToken")
    @PostMapping("/login")
    public R<LoginVO> login(@RequestBody @Validated LoginDTO loginDTO) {
        LoginVO loginVO = authService.login(loginDTO);
        return R.success(loginVO);
    }

    /**
     * 用户注册
     *
     * @param registerDTO 注册请求参数
     * @return 注册成功后的用户信息
     */
    @Operation(summary = "用户注册", description = "创建新用户，密码使用 BCrypt 加密")
    @PostMapping("/register")
    public R<UserVO> register(@RequestBody @Validated RegisterDTO registerDTO) {
        UserVO userVO = authService.register(registerDTO);
        return R.success(userVO, "注册成功");
    }

    /**
     * 刷新 AccessToken
     *
     * @param refreshTokenDTO 包含 RefreshToken 的请求
     * @return 新的 Token 对（AccessToken + RefreshToken，旧 RefreshToken 失效）
     */
    @Operation(summary = "刷新 Token", description = "使用 RefreshToken 换取新的 AccessToken 和 RefreshToken（轮换机制）")
    @PostMapping("/refresh")
    public R<TokenVO> refresh(@RequestBody @Validated RefreshTokenDTO refreshTokenDTO) {
        TokenVO tokenVO = authService.refreshToken(refreshTokenDTO.getRefreshToken());
        return R.success(tokenVO);
    }

    /**
     * 用户登出
     *
     * @param authorization Authorization 请求头（Bearer Token）
     * @param request       RefreshToken（可选，从请求体传递）
     * @return 操作结果
     */
    @Operation(summary = "用户登出", description = "将当前 Token 加入黑名单，使其失效")
    @PostMapping("/logout")
    public R<Void> logout(
            @Parameter(description = "AccessToken", hidden = true)
            @RequestHeader(value = HeaderConstants.AUTHORIZATION, required = false) String authorization,
            @RequestBody(required = false) LogoutRequest request) {

        // 解析 AccessToken
        String accessToken = null;
        if (authorization != null && authorization.startsWith(HeaderConstants.BEARER_PREFIX)) {
            accessToken = authorization.substring(HeaderConstants.BEARER_PREFIX.length());
        }

        // 解析 RefreshToken
        String refreshToken = null;
        if (request != null) {
            refreshToken = request.getRefreshToken();
        }

        authService.logout(accessToken, refreshToken);
        return R.success();
    }

    /**
     * 获取当前登录用户信息
     *
     * 用户 ID 取自 UserContext（由网关校验 JWT 后注入的 X-User-Id 请求头填充），
     * 未携带有效 Token 的请求到达不了这里（网关拦截），直连服务的请求上下文为空也会被拒绝。
     *
     * @return 用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "根据登录态查询最新用户信息")
    @GetMapping("/current")
    public R<UserVO> getCurrentUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        UserVO userVO = authService.getCurrentUser(userId);
        return R.success(userVO);
    }

    // ==================== 内部类 ====================

    /**
     * 登出请求
     */
    @Data
    public static class LogoutRequest {

        @Parameter(description = "RefreshToken（可选）")
        private String refreshToken;
    }
}
