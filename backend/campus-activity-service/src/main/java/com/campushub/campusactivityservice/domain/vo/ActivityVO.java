package com.campushub.campusactivityservice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动展示对象 VO
 *
 * 对应前端 types/activity.ts 的 Activity 结构。
 * 两个计算字段由后端填充，前端拿来即用：
 * - remainingParticipants = max - current（剩余名额）
 * - registered = 当前登录用户是否已报名（按用户维度计算，不能存库）
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

    /** 分类名称（批量查分类表填充，避免前端二次请求） */
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

    /** 当前登录用户是否已报名（计算字段：查报名表得出） */
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
