package com.campushub.campuscontentservice.controller;

import com.campushub.campuscontentservice.domain.dto.AnnouncementQueryDTO;
import com.campushub.campuscontentservice.domain.dto.AnnouncementSaveDTO;
import com.campushub.campuscontentservice.domain.vo.AnnouncementVO;
import com.campushub.campuscontentservice.service.AnnouncementService;
import com.campushub.common.response.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公告后台管理内部接口控制器
 *
 * 路径以 /internal 开头：网关未配置该前缀的路由，外部无法通过网关访问，
 * 仅供 admin-service 通过 OpenFeign 服务间调用，把公告管理能力"借"给后台。
 * 直接返回 PageResult / AnnouncementVO，不套 R 信封（R 私有构造 + final 字段，Feign 无法反序列化）。
 *
 * 与用户侧 AnnouncementController 的区别：后台能看全部（含未发布）、能增删改。
 *
 * @author CampusHub
 */
@RestController
@RequestMapping("/internal/admin/announcements")
@RequiredArgsConstructor
public class AnnouncementAdminInternalController {

    private final AnnouncementService announcementService;

    /**
     * 分页查询全部公告（含未发布），关键词命中标题/内容
     *
     * @param query 分页 + 关键词/分类参数，由查询串自动绑定
     * @return 公告分页结果
     */
    @GetMapping
    public PageResult<AnnouncementVO> page(AnnouncementQueryDTO query) {
        return announcementService.pageAllForAdmin(query);
    }

    /**
     * 新建公告：署名统一填"平台管理员"
     *
     * @param dto 公告内容（@Validated 触发校验）
     * @return 新建后的公告
     */
    @PostMapping
    public AnnouncementVO create(@RequestBody @Validated AnnouncementSaveDTO dto) {
        return announcementService.createForAdmin(dto);
    }

    /**
     * 编辑公告（标题/内容/分类/发布状态）
     *
     * @param id  公告ID
     * @param dto 公告内容
     * @return 更新后的公告
     */
    @PutMapping("/{id}")
    public AnnouncementVO update(@PathVariable Long id, @RequestBody @Validated AnnouncementSaveDTO dto) {
        return announcementService.updateForAdmin(id, dto);
    }

    /**
     * 删除公告
     *
     * @param id 公告ID
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        announcementService.deleteForAdmin(id);
    }
}
