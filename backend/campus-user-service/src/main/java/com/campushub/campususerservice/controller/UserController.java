package com.campushub.campususerservice.controller;


import com.campushub.campususerservice.client.MarketFavoriteClient;
import com.campushub.campususerservice.domain.dto.UserDTO;
import com.campushub.campususerservice.domain.vo.ProductVO;
import com.campushub.campususerservice.domain.vo.UserPublicVO;
import com.campushub.campususerservice.domain.vo.UserVO;
import com.campushub.campususerservice.service.UserService;
import com.campushub.common.context.UserContext;
import com.campushub.common.enums.ResultCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.response.PageResult;
import com.campushub.common.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 *
 * 提供当前登录用户资料的查询接口。
 * 用户身份统一从 UserContext 获取（由网关校验 JWT 后透传的 X-User-Id 填充），
 * 不接受前端传入 userId，从根本上杜绝越权访问他人数据。
 *
 * @author CampusHub
 */
@Tag(name = "用户管理", description = "当前用户资料等接口")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final MarketFavoriteClient marketFavoriteClient;

    /**
     * 获取当前登录用户资料
     *
     * @return 当前登录用户的信息
     */
    @Operation(summary = "获取当前用户", description = "返回当前登录用户的资料")
    @GetMapping("/me")
    public R<UserVO> getMe() {
        return R.success(userService.getUserById(getUserId()));
    }

    /**
     * 获取指定用户的公开信息（供商品详情页等展示"卖家/发布者介绍"与联系方式）
     *
     * 与 /me 不同，此接口允许查询他人：返回昵称/头像/简介/注册时间 + 联系方式，
     * 不含 username / role / status 等敏感字段。
     *
     * 联系方式只做登录门控（注册时手机号必填，登录用户之间互相可见）：
     * 未登录时 phone 为空串、phoneVisible=false，前端据此渲染"去登录"引导。
     *
     * @param id 目标用户 ID（路径变量，来自 /api/users/{id}/public）
     * @return 该用户的公开信息（联系方式按登录态裁剪）
     */
    @Operation(summary = "获取用户公开信息", description = "返回指定用户的公开资料；登录后可见完整联系方式")
    @GetMapping("/{id}/public")
    public R<UserPublicVO> getPublicUser(@PathVariable("id") Long id) {
        // 当前用户可能未登录（userId 为 null），可见性判定在 Service 内统一处理
        Long currentUserId = UserContext.getUserId();
        return R.success(userService.getPublicUserById(id, currentUserId));
    }

    /**
     * 更新当前用户资料
     *
     * @return 更新当前登录用户的信息
     */
    @Operation(summary = "更新当前用户资料", description = "返回更新后的用户资料")
    @PutMapping("/me")
    public R<UserVO> updateMe(@RequestBody UserDTO userDTO) {
        // 1.从上下文取当前登录用户 ID（网关认证后注入，前端无法伪造，杜绝越权）
        getUserId();

        // 2.把当前用户信息 传给 service 查询，用 R.success 包装成统一响应返回
        return R.success(userService.updateUser(userDTO));
    }

    /**
     * 我的收藏
     *
     * @return 当前登录用户收藏的商品（跨服务调用 market-service）
     */
    @Operation(summary = "获取当前用户收藏", description = "返回当前登录用户收藏的商品")
    @GetMapping("/favorites")
    public R<PageResult<ProductVO>> getMyFavorites(
            @RequestParam(value = "pageNum", defaultValue = "1") long pageNum,
            @RequestParam(value = "pageSize", defaultValue = "12") long pageSize) {
        // 1.从 JWT 上下文取当前登录用户ID（前端无法伪造，杜绝越权）
        Long userId = getUserId();

        // 2.通过 Feign"对讲机"调用 market-service 内部接口，拿到收藏商品分页数据
        PageResult<ProductVO> page = marketFavoriteClient.getFavorites(userId, pageNum, pageSize);

        // 3.用 R 信封包装后返回给前端
        return R.success(page);
    }

    /**
     * 获取当前登录用户 ID
     * @return 当前登录用户 ID
     */
    @NonNull
    private static Long getUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }
}