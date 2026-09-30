package com.campushub.campuscontentservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campushub.campuscontentservice.domain.dto.AnnouncementQueryDTO;
import com.campushub.campuscontentservice.domain.dto.AnnouncementSaveDTO;
import com.campushub.campuscontentservice.domain.entity.Announcement;
import com.campushub.campuscontentservice.domain.vo.AnnouncementVO;
import com.campushub.campuscontentservice.mapper.AnnouncementMapper;
import com.campushub.campuscontentservice.service.AnnouncementService;
import com.campushub.common.enums.ResultCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.response.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 公告服务实现类
 *
 * @author CampusHub
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    /** 首页"最新公告"展示条数 */
    private static final int LATEST_LIMIT = 5;

    /** 后台新建公告的统一署名：不接受前端传，避免伪造发布单位 */
    private static final String PLATFORM_AUTHOR = "平台管理员";

    private final AnnouncementMapper announcementMapper;

    @Override
    public PageResult<AnnouncementVO> getAnnouncements(AnnouncementQueryDTO query) {
        // 1.组装查询条件：用户侧只看已发布的（published = true）
        //   关键词用 and(...or...) 包住，避免 OR 泄漏破坏其他 AND 条件（同 activity/market 列表）
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getPublished, true)
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(Announcement::getTitle, query.getKeyword())
                        .or()
                        .like(Announcement::getContent, query.getKeyword()))
                .eq(StringUtils.hasText(query.getCategory()), Announcement::getCategory, query.getCategory())
                // 2.按发布时间倒序：最新公告排前面
                .orderByDesc(Announcement::getCreatedAt);

        Page<Announcement> page = announcementMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        List<Announcement> records = page.getRecords();
        if (records.isEmpty()) {
            return PageResult.of(List.of(), page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
        }

        // 3.转 VO 后封装统一分页结构
        List<AnnouncementVO> vos = records.stream().map(this::toVO).toList();
        return PageResult.of(vos, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    @Override
    public AnnouncementVO getAnnouncementDetail(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        // 只暴露已发布的公告：不存在或未发布都当 404（对外不区分，避免接口变成"探测未发布公告"的工具）
        if (announcement == null || !Boolean.TRUE.equals(announcement.getPublished())) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        // 浏览量 +1：原子自增（与 market 收藏计数同款），不"查出来+1再写回"，避免并发丢失更新
        announcementMapper.update(null,
                new LambdaUpdateWrapper<Announcement>()
                        .setSql("view_count = view_count + 1")
                        .eq(Announcement::getId, id));
        // 内存里的 announcement 还是自增前的旧值，手动 +1 让返回给前端的浏览量即时反映本次访问
        AnnouncementVO vo = toVO(announcement);
        vo.setViewCount(announcement.getViewCount() + 1);
        return vo;
    }

    @Override
    public List<AnnouncementVO> getLatestAnnouncements() {
        // 已发布按发布时间倒序取前 LATEST_LIMIT 条；Page(1, N) 当 LIMIT 用（同 activity upcoming / market 推荐）
        Page<Announcement> page = announcementMapper.selectPage(
                new Page<>(1, LATEST_LIMIT),
                new LambdaQueryWrapper<Announcement>()
                        .eq(Announcement::getPublished, true)
                        .orderByDesc(Announcement::getCreatedAt));
        return page.getRecords().stream().map(this::toVO).toList();
    }

    /**
     * 实体 → VO：公告无计算字段，逐字段直拷
     */
    private AnnouncementVO toVO(Announcement a) {
        AnnouncementVO vo = new AnnouncementVO();
        vo.setId(a.getId());
        vo.setTitle(a.getTitle());
        vo.setContent(a.getContent());
        vo.setCategory(a.getCategory());
        vo.setAuthor(a.getAuthor());
        vo.setPublished(a.getPublished());
        vo.setViewCount(a.getViewCount());
        vo.setCreatedAt(a.getCreatedAt());
        vo.setUpdatedAt(a.getUpdatedAt());
        return vo;
    }

    /**
     * {@inheritDoc}
     *
     * @param query 分页 + 关键词/分类参数
     * @return
     */
    @Override
    public PageResult<AnnouncementVO> pageAllForAdmin(AnnouncementQueryDTO query) {
        // 后台看全部公告（不做"已发布"过滤），其余筛选与用户侧一致
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<Announcement>()
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(Announcement::getTitle, query.getKeyword())
                        .or()
                        .like(Announcement::getContent, query.getKeyword()))
                .eq(StringUtils.hasText(query.getCategory()), Announcement::getCategory, query.getCategory())
                .orderByDesc(Announcement::getCreatedAt);

        Page<Announcement> page = announcementMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        List<AnnouncementVO> vos = page.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(vos, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    /**
     * {@inheritDoc}
     *
     * @param dto 公告内容
     * @return
     */
    @Override
    public AnnouncementVO createForAdmin(AnnouncementSaveDTO dto) {
        // 1.DTO → 实体：署名固定"平台管理员"；published 未传视为草稿
        Announcement announcement = new Announcement();
        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());
        announcement.setCategory(dto.getCategory());
        announcement.setAuthor(PLATFORM_AUTHOR);
        announcement.setPublished(Boolean.TRUE.equals(dto.getPublished()));

        // 2.入库：id/createdAt/updatedAt 由数据库填充
        announcementMapper.insert(announcement);

        // 3.回查拿到数据库填充后的完整记录再转 VO
        return toVO(announcementMapper.selectById(announcement.getId()));
    }

    /**
     * {@inheritDoc}
     *
     * @param id  公告ID
     * @param dto 公告内容
     * @return
     */
    @Override
    public AnnouncementVO updateForAdmin(Long id, AnnouncementSaveDTO dto) {
        // 1.查公告：不存在 → 404
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 2.定向更新：作者/浏览量/时间不碰（published 未传视为不下架）
        announcementMapper.update(null, new LambdaUpdateWrapper<Announcement>()
                .eq(Announcement::getId, id)
                .set(Announcement::getTitle, dto.getTitle())
                .set(Announcement::getContent, dto.getContent())
                .set(Announcement::getCategory, dto.getCategory())
                .set(dto.getPublished() != null, Announcement::getPublished, dto.getPublished()));

        // 3.回查转 VO 返回
        return toVO(announcementMapper.selectById(id));
    }

    /**
     * {@inheritDoc}
     *
     * @param id 公告ID
     */
    @Override
    public void deleteForAdmin(Long id) {
        // 1.查公告：不存在 → 404
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 2.删除
        announcementMapper.deleteById(id);
    }
}
