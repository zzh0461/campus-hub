package com.campushub.campuscontentservice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告展示对象 VO
 *
 * 对应前端 types/announcement.ts 的 Announcement 结构。
 * 公告无敏感字段，VO 与实体字段基本一致；单独设 VO 是为了与持久层解耦
 * （将来表加内部字段时不会自动泄漏给前端）
 *
 * @author CampusHub
 */
@Data
public class AnnouncementVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 公告ID */
    private Long id;

    /** 公告标题 */
    private String title;

    /** 公告内容 */
    private String content;

    /** 分类 */
    private String category;

    /** 发布单位 */
    private String author;

    /** 是否已发布 */
    private Boolean published;

    /** 浏览量 */
    private Integer viewCount;

    /** 发布时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
