package com.campushub.campusadminservice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 后台公告信息 VO（admin 侧契约副本）
 *
 * 字段与 content-service 的 AnnouncementVO、前端 types/announcement.ts 的 Announcement 对齐。
 * Feign 收到 content-service 返回的 JSON 后按字段名反序列化到本类，再套 R 返回给前端后台。
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
