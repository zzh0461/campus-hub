package com.campushub.campususerservice.client;

import com.campushub.campususerservice.domain.vo.ProductVO;
import com.campushub.common.response.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * MarketFavoriteClient 的降级备用方案（fallback）
 *
 * 当 market-service 不可用（宕机、超时、网络异常）时，Feign 断路器会改为调用本类的方法，
 * 返回一个"空的分页结果"，而不是抛异常导致 user-service 返回 500、前端显示"加载失败"。
 * 这就是"优雅降级"：宁可显示"暂无收藏"，也不要整个页面崩掉。
 *
 * 必须实现与 Feign 客户端相同的接口，方法签名一一对应。
 *
 * @author CampusHub
 */
@Slf4j
@Component
public class MarketFavoriteClientFallback implements MarketFavoriteClient {

    @Override
    public PageResult<ProductVO> getFavorites(Long userId, long pageNum, long pageSize) {
        // 记录降级日志，方便排查 market-service 为何不可用
        log.warn("调用 market-service 查询收藏失败，触发降级返回空列表: userId={}, pageNum={}, pageSize={}",
                userId, pageNum, pageSize);
        // 返回空分页结果：前端会显示"还没有收藏商品"，而不是报错崩溃
        return PageResult.of(Collections.emptyList(), 0L, pageNum, pageSize, 0L);
    }
}