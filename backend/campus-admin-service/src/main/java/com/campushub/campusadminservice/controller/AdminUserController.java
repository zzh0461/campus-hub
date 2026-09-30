package com.campushub.campusadminservice.controller;

import com.campushub.campusadminservice.client.UserAdminClient;
import com.campushub.campusadminservice.domain.dto.AdminPageQuery;
import com.campushub.campusadminservice.domain.dto.UserStatusUpdateDTO;
import com.campushub.campusadminservice.domain.vo.UserVO;
import com.campushub.common.response.PageResult;
import com.campushub.common.response.R;
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
 * 后台用户管理控制器
 *
 * 路径在 /api/admin/users/** 下，网关强制 JWT 鉴权，
 * 且 AdminAuthInterceptor 已保证到这里的一定是 ADMIN 角色，故方法内无需再校验身份。
 *
 * admin 是聚合门面：自己不碰数据库，把请求透传给 user-service 的 internal 管理接口，
 * 拿到结果后套 R 信封返回前端。
 *
 * @author CampusHub
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserAdminClient userAdminClient;

    /**
     * 用户分页列表：关键词命中用户名/昵称/手机号
     *
     * @param query 分页 + 关键词参数
     * @return 用户分页结果
     */
    @GetMapping
    public R<PageResult<UserVO>> list(AdminPageQuery query) {
        return R.success(userAdminClient.pageUsers(
                query.getKeyword(), query.getPageNum(), query.getPageSize()));
    }

    /**
     * 更新用户状态（启用/禁用），管理员账号受保护不可操作
     *
     * @param id  目标用户ID
     * @param dto 状态更新参数（@Validated 触发校验）
     * @return 更新后的用户信息
     */
    @PutMapping("/{id}/status")
    public R<UserVO> updateStatus(@PathVariable Long id,
                                  @RequestBody @Validated UserStatusUpdateDTO dto) {
        return R.success(userAdminClient.updateStatus(id, dto));
    }

    /**
     * 删除用户，管理员账号受保护不可删除
     *
     * @param id 目标用户ID
     * @return 空响应体，成功即 code=200
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        userAdminClient.delete(id);
        return R.success();
    }
}
