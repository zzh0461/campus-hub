package com.campushub.campusnotificationservice.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 站内通知实体，对应 campus_notification 表
 *
 * @author CampusHub
 */
@Data
@TableName("campus_notification")
public class Notification implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 通知ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收用户ID：通知天然属于某个用户，查询必带此条件（数据隔离） */
    private Long userId;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 类型：SYSTEM / MARKET / ACTIVITY / LOST_FOUND */
    private String type;

    /**
     * 是否已读：显式绑定 is_read 列。
     * 不叫 read 的原因：read 会被映射到不存在的 read 列，且与 Java getter 命名规则冲突
     */
    @TableField("is_read")
    private Boolean isRead;

    /** 创建时间（数据库默认 CURRENT_TIMESTAMP） */
    private LocalDateTime createdAt;
}