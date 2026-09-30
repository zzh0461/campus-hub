package com.campushub.campususerservice.controller;

import com.campushub.campususerservice.domain.dto.AdminUserQueryDTO;
import com.campushub.campususerservice.domain.dto.UserStatusUpdateDTO;
import com.campushub.campususerservice.domain.vo.UserStatsVO;
import com.campushub.campususerservice.domain.vo.UserVO;
import com.campushub.campususerservice.service.UserService;
import com.campushub.common.response.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户后台管理内部接口控制器
 *
 * 路径以 /internal 开头：网关未配置该前缀的路由，外部无法通过网关(8080)访问，
 * 仅供 admin-service 通过 OpenFeign 服务间调用，把用户管理能力"借"给后台。
 * 直接返回 PageResult / UserVO，不套 R 信封：R 是"只出不进"的（私有构造 + final 字段），
 * Feign 无法反序列化；而 PageResult/UserVO 是普通 @Data bean，可被正常反序列化。
 *
 * @author CampusHub
 */
@RestController
@RequestMapping("/internal/admin/users")
@RequiredArgsConstructor
public class UserAdminInternalController {

    private final UserService userService;

    /**
     * 分页查询用户（关键词命中用户名/昵称/手机号）
     *
     * @param query 分页 + 关键词参数，由查询串自动绑定
     * @return 用户分页结果
     */
    @GetMapping
    public PageResult<UserVO> page(AdminUserQueryDTO query) {
        return userService.pageUsersForAdmin(query);
    }

    /**
     * 更新用户状态（启用/禁用），管理员账号受保护不可操作
     *
     * @param id  目标用户ID
     * @param dto 状态更新参数（@Validated 触发校验）
     * @return 更新后的用户信息
     */
    @PutMapping("/{id}/status")
    public UserVO updateStatus(@PathVariable Long id,
                               @RequestBody @Validated UserStatusUpdateDTO dto) {
        return userService.updateUserStatusByAdmin(id, dto);
    }

    /**
     * 用户统计：总数 + 今日新增 + 近 14 天注册趋势（供 Dashboard 聚合）
     *
     * @return 用户统计
     */
    @GetMapping("/stats")
    public UserStatsVO stats() {
        return userService.getUserStatsForAdmin();
    }

    /**
     * 删除用户，管理员账号受保护不可删除
     *
     * @param id 目标用户ID
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userService.deleteUserByAdmin(id);
    }
}
