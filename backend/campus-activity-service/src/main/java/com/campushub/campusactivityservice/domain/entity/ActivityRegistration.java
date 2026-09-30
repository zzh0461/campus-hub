package com.campushub.campusactivityservice.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动报名记录实体，对应 campus_activity_registration 表
 *
 * 表上有 uk_activity_user(activity_id, user_id) 唯一约束——
 * 防重复报名的数据库级最后防线（跟 market 收藏表同款设计），
 * 应用层先查后插只是优化体验，并发下的兜底靠它
 *
 * @author CampusHub
 */
@Data
@TableName("campus_activity_registration")
public class ActivityRegistration implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 活动ID */
    private Long activityId;

    /** 报名用户ID */
    private Long userId;

    /** 报名时间（数据库默认 CURRENT_TIMESTAMP） */
    private LocalDateTime createdAt;
}
