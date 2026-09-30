package com.campushub.campusadminservice.controller;

import com.campushub.campusadminservice.client.AnnouncementAdminClient;
import com.campushub.campusadminservice.domain.dto.AdminPageQuery;
import com.campushub.campusadminservice.domain.dto.AnnouncementSaveDTO;
import com.campushub.campusadminservice.domain.vo.AnnouncementVO;
import com.campushub.common.response.PageResult;
import com.campushub.common.response.R;
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
 * 后台公告管理控制器
 *
 * 路径在 /api/admin/announcements/** 下，网关强制 JWT 鉴权，
 * 且 AdminAuthInterceptor 已保证到这里的一定是 ADMIN 角色，故方法内无需再校验身份。
 *
 * admin 是聚合门面：自己不碰数据库，把请求透传给 content-service 的 internal 管理接口，
 * 拿到结果后套 R 信封返回前端。
 *
 * @author CampusHub
 */
@RestController
@RequestMapping("/api/admin/announcements")
@RequiredArgsConstructor
public class AdminAnnouncementController {

    private final AnnouncementAdminClient announcementAdminClient;

    /**
     * 公告分页列表：含未发布草稿，关键词命中标题/内容
     *
     * @param query 分页 + 关键词参数
     * @return 公告分页结果
     */
    @GetMapping
    public R<PageResult<AnnouncementVO>> list(AdminPageQuery query) {
        return R.success(announcementAdminClient.page(
                query.getPageNum(), query.getPageSize(), query.getKeyword()));
    }

    /**
     * 新建公告：作者由 content-service 统一署名"平台管理员"
     *
     * @param dto 公告内容（@Validated 触发校验）
     * @return 新建后的公告
     */
    @PostMapping
    public R<AnnouncementVO> create(@RequestBody @Validated AnnouncementSaveDTO dto) {
        return R.success(announcementAdminClient.create(dto));
    }

    /**
     * 编辑公告（标题/内容/分类/发布状态）
     *
     * @param id  公告ID
     * @param dto 公告内容
     * @return 更新后的公告
     */
    @PutMapping("/{id}")
    public R<AnnouncementVO> update(@PathVariable Long id,
                                    @RequestBody @Validated AnnouncementSaveDTO dto) {
        return R.success(announcementAdminClient.update(id, dto));
    }

    /**
     * 删除公告
     *
     * @param id 公告ID
     * @return 空响应体，成功即 code=200
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        announcementAdminClient.delete(id);
        return R.success();
    }
}
