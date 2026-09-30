package com.campushub.campusactivityservice.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 校园活动实体，对应 campus_activity 表
 *
 * @author CampusHub
 */
@Data
@TableName("campus_activity")
public class Activity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 活动ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 活动标题 */
    private String title;

    /** 活动介绍 */
    private String description;

    /** 分类ID（campus_activity_category 表，跨表无外键，微服务惯例靠代码维护一致性） */
    private Long categoryId;

    /** 活动地点 */
    private String location;

    /** 封面图 URL */
    private String cover;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 人数上限（名额） */
    private Integer maxParticipants;

    /** 当前报名人数：报名/取消时用 SQL 原子增减维护（与 market 的 favorite_count 同款套路） */
    private Integer currentParticipants;

    /** 主办方名称（冗余文本，如"校团委·学生会"） */
    private String organizer;

    /** 主办方用户ID */
    private Long organizerId;

    /** 状态：UPCOMING(未开始) / ONGOING(进行中) / FINISHED(已结束)，由定时任务流转 */
    private String status;

    /** 创建时间（数据库默认 CURRENT_TIMESTAMP） */
    private LocalDateTime createdAt;

    /** 更新时间（数据库 ON UPDATE CURRENT_TIMESTAMP 自动维护） */
    private LocalDateTime updatedAt;
}
