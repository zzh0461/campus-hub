package com.campushub.campusactivityservice.client;

import com.campushub.campusactivityservice.domain.vo.UserBriefVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * UserClient 的降级备用方案（fallback）
 *
 * 当 user-service 不可用（宕机、超时、网络异常）时，Feign 断路器改为调用本类方法，
 * 返回"空列表"而不是抛异常——发布活动时拿不到昵称，就退回通用署名，
 * 不能让"查个昵称"拖垮发布主链路（与 content-service 的 UserClientFallback 同一哲学）。
 *
 * @author CampusHub
 */
@Slf4j
@Component
public class UserClientFallback implements UserClient {

    @Override
    public List<UserBriefVO> listByIds(List<Long> ids) {
        log.warn("调用 user-service 批量查用户失败，触发降级返回空列表: ids={}", ids);
        return Collections.emptyList();
    }
}
