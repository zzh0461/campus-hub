package com.campushub.campusactivityservice.client;

import com.campushub.campusactivityservice.domain.vo.UserBriefVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * user-service "批量查用户接口"的 Feign 客户端（服务间"对讲机"）
 *
 * name = "campus-user-service"：目标服务在 Nacos 的服务名，Feign 靠它做服务发现 + 负载均衡。
 * path = "/internal/users"：目标接口公共前缀（与 user-service 的 UserInternalController 对齐）。
 * 返回 List<UserBriefVO>：与 user 内部接口返回结构一致（不套 R，因为 R 无法反序列化）。
 *
 * activity 用它把发布者昵称填进 organizer——活动表只存 organizer_id，昵称是 user 域的数据。
 *
 * @author CampusHub
 */
@FeignClient(
        name = "campus-user-service",
        contextId = "userBriefClient",
        path = "/internal/users",
        fallback = UserClientFallback.class // user-service 不可用时启用的降级备用方案
)
public interface UserClient {

    /**
     * 批量查询用户简要信息（用于把发布者昵称填进主办方字段）
     *
     * @param ids 用户ID列表
     * @return 用户简要信息列表
     */
    @PostMapping("/batch")
    List<UserBriefVO> listByIds(@RequestBody List<Long> ids);
}
