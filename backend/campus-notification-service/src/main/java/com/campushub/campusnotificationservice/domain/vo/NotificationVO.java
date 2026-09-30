package com.campushub.campusnotificationservice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知展示对象 VO
 *
 * 对应前端 types/notification.ts 的 Notification 结构。
 * 不含 userId：通知永远只属于当前登录者，返回它没有意义（VO 只暴露展示所需字段）
 *
 * @author CampusHub
 */
@Data
public class NotificationVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 通知ID */
    private Long id;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 类型：SYSTEM / MARKET / ACTIVITY / LOST_FOUND（前端据此显示图标/颜色） */
    private String type;

    /** 是否已读（对齐前端契约字段名 read） */
    private Boolean read;

    /** 创建时间 */
    private LocalDateTime createdAt;
}