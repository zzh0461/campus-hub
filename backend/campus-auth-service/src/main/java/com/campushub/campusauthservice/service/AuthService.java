package com.campushub.campusauthservice.service;

import com.campushub.campusauthservice.domain.dto.LoginDTO;
import com.campushub.campusauthservice.domain.dto.RegisterDTO;
import com.campushub.campusauthservice.domain.vo.LoginVO;
import com.campushub.campusauthservice.domain.vo.TokenVO;
import com.campushub.campusauthservice.domain.vo.UserVO;

/**
 * 认证服务接口
 *
 * 定义用户认证相关的业务方法，包括登录、注册、Token 刷新、登出等。
 *
 * @author CampusHub
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * 验证用户名和密码，生成 AccessToken 和 RefreshToken。
     * AccessToken 返回给前端，RefreshToken 存储到 Redis。
     *
     * @param loginDTO 登录请求参数
     * @return 登录结果（AccessToken、RefreshToken、用户信息）
     * @throws com.campushub.common.exception.BusinessException 登录失败时抛出
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 用户注册
     *
     * 创建新用户，密码使用 BCrypt 加密存储。
     *
     * @param registerDTO 注册请求参数
     * @return 注册成功后的用户信息（不含密码）
     * @throws com.campushub.common.exception.BusinessException 用户名已存在时抛出
     */
    UserVO register(RegisterDTO registerDTO);

    /**
     * 刷新 AccessToken
     *
     * 使用 RefreshToken 获取新的 AccessToken 和新的 RefreshToken（轮换机制）。
     * RefreshToken 必须在 Redis 中存在且未过期；旧 RefreshToken 刷新后立即失效。
     *
     * @param refreshToken RefreshToken 字符串
     * @return 新的 Token 对（AccessToken + RefreshToken）
     * @throws com.campushub.common.exception.BusinessException RefreshToken 无效或已过期时抛出
     */
    TokenVO refreshToken(String refreshToken);

    /**
     * 用户登出
     *
     * 将 AccessToken 和 RefreshToken 加入 Redis 黑名单，使其失效。
     *
     * @param accessToken  当前的 AccessToken
     * @param refreshToken 当前的 RefreshToken（可选）
     */
    void logout(String accessToken, String refreshToken);

    /**
     * 获取当前登录用户信息
     *
     * 根据用户 ID 从数据库查询最新的用户信息。
     *
     * @param userId 用户 ID
     * @return 用户信息（不含密码）
     */
    UserVO getCurrentUser(Long userId);
}
