package com.campushub.campuscontentservice.controller;

import com.campushub.campuscontentservice.domain.dto.LostFoundQueryDTO;
import com.campushub.campuscontentservice.domain.dto.LostFoundStatusUpdateDTO;
import com.campushub.campuscontentservice.domain.vo.ContentStatsVO;
import com.campushub.campuscontentservice.domain.vo.LostFoundVO;
import com.campushub.campuscontentservice.service.LostFoundService;
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
 * 失物招领后台管理内部接口控制器
 *
 * /internal 前缀不经网关，仅供 admin-service Feign 调用；返回裸对象不套 R。
 * 列表直接复用用户侧 getLostFoundList（本就返回全部记录并填充发布者名片）；
 * 改状态/删除走"无归属校验"的后台专用方法（管理员可处理任意人的记录）；
 * /stats 提供失物招领统计，供后台 Dashboard 饼图使用。
 *
 * @author CampusHub
 */
@RestController
@RequestMapping("/internal/admin/lost-found")
@RequiredArgsConstructor
public class LostFoundAdminInternalController {

    private final LostFoundService lostFoundService;

    /**
     * 分页查询全部失物招领（关键词命中标题/描述）
     *
     * @param query 分页 + 关键词/类型参数
     * @return 失物招领分页结果
     */
    @GetMapping
    public PageResult<LostFoundVO> page(LostFoundQueryDTO query) {
        return lostFoundService.getLostFoundList(query);
    }

    /**
     * 后台改状态（OPEN ↔ RESOLVED），无需发布者归属校验
     *
     * @param id  记录ID
     * @param dto 状态更新参数
     * @return 更新后的记录
     */
    @PutMapping("/{id}/status")
    public LostFoundVO updateStatus(@PathVariable Long id,
                                    @RequestBody @Validated LostFoundStatusUpdateDTO dto) {
        return lostFoundService.updateLostFoundStatusByAdmin(id, dto);
    }

    /**
     * 后台删除任意失物招领记录
     *
     * @param id 记录ID
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        lostFoundService.deleteLostFoundByAdmin(id);
    }

    /**
     * 失物招领统计：总数 + 失物/招领分类计数（供 Dashboard）
     *
     * @return 内容统计
     */
    @GetMapping("/stats")
    public ContentStatsVO stats() {
        return lostFoundService.getContentStats();
    }
}
