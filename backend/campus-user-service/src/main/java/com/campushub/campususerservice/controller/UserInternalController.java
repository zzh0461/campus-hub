package com.campushub.campususerservice.controller;

import com.campushub.campususerservice.domain.vo.UserBriefVO;
import com.campushub.campususerservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户内部接口控制器
 *
 * 路径以 /internal 开头：网关未配置该前缀的路由，外部无法通过网关(8080)访问，
 * 仅供其它服务（如 market-service）通过 OpenFeign 服务间调用。
 * 直接返回 List，不套 R 信封：R 是"只出不进"的（私有构造 + final 字段），Feign 无法反序列化。
 *
 * @author CampusHub
 */
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class UserInternalController {

    private final UserService userService;

    /**
     * 批量查询用户简要信息（供其它服务填充展示字段，如卖家昵称/头像）
     *
     * @param ids 用户ID列表
     * @return 用户简要信息列表（查不到的 id 会被自然忽略）
     */
    @PostMapping("/batch")
    public List<UserBriefVO> listByIds(@RequestBody List<Long> ids) {
        return userService.listBriefByIds(ids);
    }
}