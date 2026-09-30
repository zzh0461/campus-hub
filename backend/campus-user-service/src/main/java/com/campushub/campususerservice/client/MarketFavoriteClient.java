package com.campushub.campususerservice.client;

import com.campushub.campususerservice.domain.vo.ProductVO;
import com.campushub.common.response.PageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * market-service "收藏接口"的 Feign 客户端（服务间"对讲机"）
 *
 * name = "campus-market-service"：目标服务在 Nacos 的服务名，Feign 靠它做服务发现 + 负载均衡，直连不经过网关。
 * path = "/internal/market"：目标接口公共前缀。
 * 返回 PageResult<ProductVO>：与 market 内部接口返回结构一致（不套 R，因为 R 无法反序列化）。
 *
 * @author CampusHub
 */
@FeignClient(
        name = "campus-market-service",
        path = "/internal/market",
        fallback = MarketFavoriteClientFallback.class // market-service 不可用时启用的降级备用方案
)
public interface MarketFavoriteClient {

    /**
     * 查询指定用户收藏的商品（分页）
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 收藏商品分页数据
     */
    @GetMapping("/favorites")
    PageResult<ProductVO> getFavorites(
            @RequestParam("userId") Long userId,
            @RequestParam("pageNum") long pageNum,
            @RequestParam("pageSize") long pageSize);
}