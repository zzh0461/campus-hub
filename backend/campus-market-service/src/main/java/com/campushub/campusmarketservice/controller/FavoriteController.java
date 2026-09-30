package com.campushub.campusmarketservice.controller;

import com.campushub.campusmarketservice.domain.vo.ProductVO;
import com.campushub.campusmarketservice.service.FavoriteService;
import com.campushub.common.response.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品收藏控制器（内部接口）
 *
 * 路径以 /internal 开头：网关未配置该前缀的路由，外部无法通过网关(8080)访问，
 * 仅供 user-service 通过 OpenFeign 服务间调用。
 * userId 由调用方从已验证的 JWT 中取得后传入，可信，杜绝越权查看他人收藏。
 *
 * @author CampusHub
 */
@RestController
@RequestMapping("/internal/market")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 分页查询指定用户收藏的商品
     *
     * @param userId   用户ID（由 user-service 从 JWT 解析后传入）
     * @param pageNum  页码，默认 1
     * @param pageSize 每页条数，默认 10
     * @return 收藏商品分页结果
     */
    @GetMapping("/favorites")
    public PageResult<ProductVO> getFavorites(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "pageNum", defaultValue = "1") long pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") long pageSize) {
        // 内部接口直接返回分页数据，不套 R 信封：
        // R 是"只出不进"的（私有构造 + final 字段），Feign 无法反序列化；PageResult 有无参构造器，可以。
        return favoriteService.getMyFavorites(userId, pageNum, pageSize);
    }
}