package com.campushub.campusauthservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campushub.campusauthservice.domain.dto.LoginDTO;
import com.campushub.campusauthservice.domain.dto.RegisterDTO;
import com.campushub.campusauthservice.domain.entity.User;
import com.campushub.campusauthservice.domain.vo.LoginVO;
import com.campushub.campusauthservice.domain.vo.TokenVO;
import com.campushub.campusauthservice.domain.vo.UserVO;
import com.campushub.campusauthservice.mapper.UserMapper;
import com.campushub.campusauthservice.service.AuthService;
import com.campushub.campusauthservice.util.JwtUtil;
import com.campushub.common.constant.RedisConstants;
import com.campushub.common.enums.ResultCode;
import com.campushub.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 *
 * 实现 AccessToken + RefreshToken + Redis 的认证机制：
 * 1. 登录成功后生成两个 Token，RefreshToken 存入 Redis（同一用户仅保留最新会话）
 * 2. 前端使用 AccessToken 请求接口，过期后用 RefreshToken 换取新的 AccessToken
 * 3. 刷新时轮换 RefreshToken：旧 RefreshToken 立即失效（黑名单 + Redis 覆盖）
 * 4. 登出时将两个 Token 按剩余有效期加入 Redis 黑名单，网关据此拦截请求
 *
 * @author CampusHub
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * BCrypt 密码编码器：线程安全，可全局共享
     */
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    /**
     * 连续登录失败次数上限，达到后锁定账号
     */
    private static final int MAX_LOGIN_FAIL_COUNT = 5;

    /**
     * 登录失败锁定时长
     */
    private static final Duration LOGIN_LOCK_DURATION = Duration.ofMinutes(15);

    /**
     * Token 类型前缀（与前端 Authorization 请求头约定一致）
     */
    private static final String TOKEN_TYPE_BEARER = "Bearer";

    /**
     * {@inheritDoc}
     */
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 1. 登录失败次数过多时直接拒绝（防暴力破解）
        String failKey = RedisConstants.LOGIN_FAIL_KEY_PREFIX + loginDTO.getUsername();
        String failCount = stringRedisTemplate.opsForValue().get(failKey);
        if (failCount != null && Integer.parseInt(failCount) >= MAX_LOGIN_FAIL_COUNT) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "登录失败次数过多，请 " + LOGIN_LOCK_DURATION.toMinutes() + " 分钟后再试");
        }

        // 2. 查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = userMapper.selectOne(queryWrapper);

        // 3. 验证密码（BCrypt）——用户不存在与密码错误返回一致文案，不泄漏账号是否存在
        if (user == null || !PASSWORD_ENCODER.matches(loginDTO.getPassword(), user.getPasswordHash())) {
            recordLoginFail(failKey);
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }

        // 4. 验证用户状态（放在密码校验之后，避免未通过验证即可探测账号状态）
        if ("DISABLED".equals(user.getStatus())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用，请联系管理员");
        }

        // 5. 生成 Token
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername(), user.getRole());

        // 6. 将 RefreshToken 存入 Redis（用于后续刷新，覆盖旧值实现单会话）
        String refreshKey = RedisConstants.REFRESH_TOKEN_KEY_PREFIX + user.getId();
        redisTemplate.opsForValue().set(refreshKey, refreshToken,
                jwtUtil.getRefreshTokenExpiration(), TimeUnit.MILLISECONDS);

        // 7. 登录成功，清除失败计数
        stringRedisTemplate.delete(failKey);

        // 8. 构建返回结果
        LoginVO loginVO = new LoginVO();
        loginVO.setAccessToken(accessToken);
        loginVO.setRefreshToken(refreshToken);
        loginVO.setExpiresIn(jwtUtil.getAccessTokenExpiration() / 1000);
        // 与前端契约（LoginResult: token / tokenType / user）对齐
        loginVO.setToken(accessToken);
        loginVO.setTokenType(TOKEN_TYPE_BEARER);
        loginVO.setUser(convertToUserVO(user));

        log.info("用户登录成功: username={}, userId={}", user.getUsername(), user.getId());
        return loginVO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserVO register(RegisterDTO registerDTO) {
        // 0. 两次密码一致性兜底校验（前端已校验，防止绕过前端直接调接口）
        if (registerDTO.getConfirmPassword() != null
                && !registerDTO.getConfirmPassword().equals(registerDTO.getPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "两次输入的密码不一致");
        }

        // 1. 快速失败：用户名已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, registerDTO.getUsername());
        Long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名已存在");
        }

        // 2. 创建用户实体
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPasswordHash(PASSWORD_ENCODER.encode(registerDTO.getPassword()));
        user.setNickname(registerDTO.getNickname());
        user.setPhone(registerDTO.getPhone());
        user.setRole("USER");
        user.setStatus("ACTIVE");

        // 3. 插入数据库：并发场景由 uk_username 唯一键兜底，冲突时转为业务异常
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名已存在");
        }

        log.info("用户注册成功: username={}, userId={}", user.getUsername(), user.getId());
        return convertToUserVO(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TokenVO refreshToken(String refreshToken) {
        // 1. 验证 RefreshToken 有效性（签名 + 过期 + 类型）
        if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "RefreshToken 无效或已过期");
        }

        // 2. 检查 Token 是否在黑名单中（已被轮换或登出）
        if (isTokenBlacklisted(refreshToken)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "RefreshToken 已失效，请重新登录");
        }

        // 3. 从 Token 中获取用户信息
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        String username = jwtUtil.getUsernameFromToken(refreshToken);
        String role = jwtUtil.getRoleFromToken(refreshToken);

        // 4. 验证 Redis 中的 RefreshToken 是否匹配（防止使用过期会话的旧 Token）
        String refreshKey = RedisConstants.REFRESH_TOKEN_KEY_PREFIX + userId;
        Object storedRefreshToken = redisTemplate.opsForValue().get(refreshKey);
        if (storedRefreshToken == null || !refreshToken.equals(storedRefreshToken.toString())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "RefreshToken 已失效，请重新登录");
        }

        // 5. 轮换 RefreshToken：签发新 RefreshToken 并覆盖 Redis 中的旧值，旧 Token 加入黑名单立即失效
        String newAccessToken = jwtUtil.generateAccessToken(userId, username, role);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId, username, role);
        redisTemplate.opsForValue().set(refreshKey, newRefreshToken,
                jwtUtil.getRefreshTokenExpiration(), TimeUnit.MILLISECONDS);
        blacklistToken(refreshToken);

        log.info("Token 刷新成功: userId={}", userId);

        TokenVO tokenVO = new TokenVO();
        tokenVO.setAccessToken(newAccessToken);
        tokenVO.setRefreshToken(newRefreshToken);
        tokenVO.setExpiresIn(jwtUtil.getAccessTokenExpiration() / 1000);
        return tokenVO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logout(String accessToken, String refreshToken) {
        // 1. 将 AccessToken 按剩余有效期加入黑名单，网关校验时直接拒绝
        if (accessToken != null && jwtUtil.validateToken(accessToken)) {
            blacklistToken(accessToken);
            Long userId = jwtUtil.getUserIdFromToken(accessToken);
            // 2. 删除 Redis 中的 RefreshToken，使其无法再刷新
            String refreshKey = RedisConstants.REFRESH_TOKEN_KEY_PREFIX + userId;
            redisTemplate.delete(refreshKey);
            log.info("用户登出成功: userId={}", userId);
        }

        // 3. RefreshToken 同样加入黑名单（单独携带 RefreshToken 登出的场景），并删除 Redis 会话
        if (refreshToken != null && jwtUtil.validateToken(refreshToken) && jwtUtil.isRefreshToken(refreshToken)) {
            blacklistToken(refreshToken);
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);
            redisTemplate.delete(RedisConstants.REFRESH_TOKEN_KEY_PREFIX + userId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserVO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return convertToUserVO(user);
    }

    /**
     * 记录一次登录失败（首次失败时设置过期时间）
     *
     * @param failKey 登录失败计数 Key
     */
    private void recordLoginFail(String failKey) {
        Long count = stringRedisTemplate.opsForValue().increment(failKey);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(failKey, LOGIN_LOCK_DURATION);
        }
    }

    /**
     * 将 Token 加入 Redis 黑名单，TTL 为 Token 的剩余有效期
     * <p>过期后黑名单自动清理，Redis 中不会累积无用数据。</p>
     *
     * @param token Token 字符串
     */
    private void blacklistToken(String token) {
        long remaining = jwtUtil.getRemainingValidity(token);
        if (remaining <= 0) {
            return;
        }
        String blacklistKey = RedisConstants.TOKEN_BLACKLIST_KEY_PREFIX + token;
        redisTemplate.opsForValue().set(blacklistKey, "1", remaining, TimeUnit.MILLISECONDS);
    }

    /**
     * 检查 Token 是否在黑名单中
     *
     * @param token Token 字符串
     * @return true=在黑名单中，false=不在
     */
    private boolean isTokenBlacklisted(String token) {
        String blacklistKey = RedisConstants.TOKEN_BLACKLIST_KEY_PREFIX + token;
        return redisTemplate.hasKey(blacklistKey);
    }

    /**
     * 将 User 实体转换为 UserVO
     *
     * @param user 用户实体
     * @return 用户 VO
     */
    private UserVO convertToUserVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }
}
