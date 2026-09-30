package com.campushub.campuscontentservice.controller;

import com.campushub.campuscontentservice.domain.dto.AnnouncementQueryDTO;
import com.campushub.campuscontentservice.domain.vo.AnnouncementVO;
import com.campushub.campuscontentservice.service.AnnouncementService;
import com.campushub.common.response.PageResult;
import com.campushub.common.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公告控制器
 *
 * 路径在 /api/announcements/** 下，网关已配好路由（强制 JWT 鉴权）。
 * 公告是纯读的公共内容，不含用户个性化数据，故无需从 UserContext 取 userId
 *
 * @author CampusHub
 */
@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * 公告分页列表：关键词/分类筛选，只返回已发布的，按发布时间倒序
     *
     * @param query 分页 + 筛选参数
     * @return 公告分页结果
     */
    @GetMapping
    public R<PageResult<AnnouncementVO>> list(AnnouncementQueryDTO query) {
        return R.success(announcementService.getAnnouncements(query));
    }

    /**
     * 首页最新公告（非分页）。
     * /latest 是字面量路径，匹配优先于 /{id} 路径变量，不会被误当成 id="latest" 的详情请求
     *
     * @return 最新公告列表
     */
    @GetMapping("/latest")
    public R<List<AnnouncementVO>> latest() {
        return R.success(announcementService.getLatestAnnouncements());
    }

    /**
     * 公告详情：访问后浏览量 +1
     *
     * @param id 公告ID
     * @return 公告详情
     */
    @GetMapping("/{id}")
    public R<AnnouncementVO> detail(@PathVariable Long id) {
        return R.success(announcementService.getAnnouncementDetail(id));
    }
}
