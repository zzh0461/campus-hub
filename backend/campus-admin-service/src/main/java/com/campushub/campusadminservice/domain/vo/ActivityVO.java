package com.campushub.campusadminservice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 后台活动信息 VO（admin 侧契约副本）
 *
 * 字段与 activity-service 的 ActivityVO、前端 types/activity.ts 的 Activity 对齐。
 * 后台列表的 registered 恒为 false（无"当前用户"语义）。
 *
 * @author CampusHub
 */
@Data
public class ActivityVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 活动ID */
    private Long id;

    /** 活动标题 */
    private String title;

    /** 活动介绍 */
    private String description;

    /** 分类ID */
    private Long categoryId;

    /** 分类名称 */
    private String categoryName;

    /** 活动地点 */
    private String location;

    /** 封面图 URL */
    private String cover;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 人数上限 */
    private Integer maxParticipants;

    /** 当前报名人数 */
    private Integer currentParticipants;

    /** 剩余名额（计算字段：max - current，最小 0） */
    private Integer remainingParticipants;

    /** 是否已报名（后台列表恒为 false） */
    private Boolean registered;

    /** 主办方名称 */
    private String organizer;

    /** 主办方用户ID */
    private Long organizerId;

    /** 状态：UPCOMING / ONGOING / FINISHED */
    private String status;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
