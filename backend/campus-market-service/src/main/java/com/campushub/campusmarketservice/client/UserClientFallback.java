package com.campushub.campusmarketservice.client;

import com.campushub.campusmarketservice.domain.vo.UserBriefVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * UserClient 的降级备用方案（fallback）
 *
 * 当 user-service 不可用（宕机、超时、网络异常）时，Feign 断路器会改为调用本类的方法，
 * 返回一个"空列表"，而不是抛异常导致 market-service 返回 500、前端商品列表整页崩掉。
 * 这就是"优雅降级"：宁可卡片上卖家名暂时空白，也不要整个二手市场页打不开。
 *
 * 必须实现与 Feign 客户端相同的接口，方法签名一一对应。
 *
 * @author CampusHub
 */
@Slf4j
@Component
public class UserClientFallback implements UserClient {

    @Override
    public List<UserBriefVO> listByIds(List<Long> ids) {
        // 记录降级日志，方便排查 user-service 为何不可用
        log.warn("调用 user-service 批量查用户失败，触发降级返回空列表: ids={}", ids);
        // 返回空列表：商品卡片卖家名会空白，但列表照常显示，不崩
        return Collections.emptyList();
    }
}