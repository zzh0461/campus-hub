package com.campushub.campusmarketservice.controller;

import com.campushub.campusmarketservice.domain.vo.FavoriteResultVO;
import com.campushub.campusmarketservice.service.FavoriteService;
import com.campushub.common.context.UserContext;
import com.campushub.common.enums.ResultCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

/**
 * 商品收藏控制器（对外公开接口）
 *
 * 路径在 /api/market/** 下，由网关路由到本服务，且需 JWT 鉴权。
 * 用户身份统一从 UserContext 获取（网关校验 JWT 后透传的 X-User-Id 填充），
 * 不接受前端传入 userId，杜绝越权取消他人收藏。
 *
 * @author CampusHub
 */
@RestController
@RequestMapping("/api/market/products")
@RequiredArgsConstructor
public class ProductFavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 取消收藏商品
     *
     * @param id 商品ID（来自 URL 路径）
     * @return { favorite: false }
     */
    @DeleteMapping("/{id}/favorite")
    public R<FavoriteResultVO> unfavorite(@PathVariable("id") Long id) {
        // 从 JWT 上下文取当前登录用户ID（前端无法伪造，杜绝越权）
        Long userId = getUserId();
        // 委托 service 执行"取消收藏"业务
        favoriteService.unfavorite(userId, id);
        // 返回操作后的收藏状态：false 表示已取消
        return R.success(new FavoriteResultVO(false));
    }

    /**
     * 收藏商品
     *
     * @param id 商品ID（来自 URL 路径）
     * @return { favorite: true }
     */
    @PostMapping("/{id}/favorite")
    public R<FavoriteResultVO> favorite(@PathVariable("id") Long id) {
        // 同样从 JWT 上下文取用户ID，杜绝越权收藏
        Long userId = getUserId();
        // 委托 service 执行"收藏"业务（内部已做幂等保护）
        favoriteService.favorite(userId, id);
        // 返回操作后的收藏状态：true 表示已收藏，代码执行到这，商品是必然已收藏的，所以直接返回true
        return R.success(new FavoriteResultVO(true));
    }

    /**
     * 获取当前登录用户ID，未登录则抛 401
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