package com.campushub.campusadminservice.controller;

import com.campushub.campusadminservice.client.LostFoundAdminClient;
import com.campushub.campusadminservice.domain.dto.AdminPageQuery;
import com.campushub.campusadminservice.domain.dto.LostFoundStatusUpdateDTO;
import com.campushub.campusadminservice.domain.vo.LostFoundVO;
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
 * 后台失物招领管理控制器
 *
 * 路径在 /api/admin/lost-found/** 下，网关强制 JWT 鉴权，
 * 且 AdminAuthInterceptor 已保证到这里的一定是 ADMIN 角色，故方法内无需再校验身份。
 *
 * admin 是聚合门面：自己不碰数据库，把请求透传给 content-service 的 internal 管理接口，
 * 拿到结果后套 R 信封返回前端。
 *
 * @author CampusHub
 */
@RestController
@RequestMapping("/api/admin/lost-found")
@RequiredArgsConstructor
public class AdminLostFoundController {

    private final LostFoundAdminClient lostFoundAdminClient;

    /**
     * 失物招领分页列表：关键词命中标题/描述
     *
     * @param query 分页 + 关键词参数
     * @return 失物招领分页结果
     */
    @GetMapping
    public R<PageResult<LostFoundVO>> list(AdminPageQuery query) {
        return R.success(lostFoundAdminClient.page(
                query.getPageNum(), query.getPageSize(), query.getKeyword()));
    }

    /**
     * 更新失物招领状态（OPEN ↔ RESOLVED），无需发布者归属校验
     *
     * @param id  记录ID
     * @param dto 状态更新参数（@Validated 触发校验）
     * @return 更新后的记录
     */
    @PutMapping("/{id}/status")
    public R<LostFoundVO> updateStatus(@PathVariable Long id,
                                       @RequestBody @Validated LostFoundStatusUpdateDTO dto) {
        return R.success(lostFoundAdminClient.updateStatus(id, dto));
    }

    /**
     * 删除任意失物招领记录
     *
     * @param id 记录ID
     * @return 空响应体，成功即 code=200
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        lostFoundAdminClient.delete(id);
        return R.success();
    }
}
