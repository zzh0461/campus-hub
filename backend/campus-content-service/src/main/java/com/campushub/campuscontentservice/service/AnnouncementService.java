package com.campushub.campuscontentservice.service;

import com.campushub.campuscontentservice.domain.dto.AnnouncementQueryDTO;
import com.campushub.campuscontentservice.domain.dto.AnnouncementSaveDTO;
import com.campushub.campuscontentservice.domain.vo.AnnouncementVO;
import com.campushub.common.response.PageResult;

import java.util.List;

/**
 * 公告服务接口
 *
 * @author CampusHub
 */
public interface AnnouncementService {

    /**
     * 分页查询已发布公告（关键词/分类筛选），按发布时间倒序
     *
     * @param query 分页 + 筛选参数
     * @return 公告分页结果
     */
    PageResult<AnnouncementVO> getAnnouncements(AnnouncementQueryDTO query);

    /**
     * 公告详情：只暴露已发布的，访问后浏览量原子 +1；不存在或未发布抛 404
     *
     * @param id 公告ID
     * @return 公告详情（viewCount 已反映本次访问）
     */
    AnnouncementVO getAnnouncementDetail(Long id);

    /**
     * 首页最新公告：已发布按发布时间倒序取前几条（非分页）
     *
     * @return 最新公告列表
     */
    List<AnnouncementVO> getLatestAnnouncements();

    /**
     * 后台分页查询全部公告（供 admin-service 调用）
     *
     * 与用户侧 getAnnouncements 的区别：不做"已发布"过滤，草稿也返回。
     *
     * @param query 分页 + 关键词/分类参数
     * @return 公告分页结果
     */
    PageResult<AnnouncementVO> pageAllForAdmin(AnnouncementQueryDTO query);

    /**
     * 后台新建公告：署名统一填"平台管理员"，不接受前端传
     *
     * @param dto 公告内容
     * @return 新建后的公告
     */
    AnnouncementVO createForAdmin(AnnouncementSaveDTO dto);

    /**
     * 后台编辑公告（标题/内容/分类/发布状态）；公告不存在抛 404
     *
     * @param id  公告ID
     * @param dto 公告内容
     * @return 更新后的公告
     */
    AnnouncementVO updateForAdmin(Long id, AnnouncementSaveDTO dto);

    /**
     * 后台删除公告；公告不存在抛 404
     *
     * @param id 公告ID
     */
    void deleteForAdmin(Long id);
}
