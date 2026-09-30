package com.campushub.campusactivityservice.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 活动被报名事件（MQ 消息体，JSON 序列化）
 *
 * 只描述"发生了什么"（业务事实），不包含通知标题/文案——
 * 通知长什么样是 notification-service 的职责，两边靠 JSON 字段名对齐契约，
 * 各自持有本类副本，不通过 common 模块共享（服务自治，避免公共模块膨胀）
 *
 * @author CampusHub
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityRegisteredEvent {

    /** 活动组织者用户ID（通知接收人） */
    private Long organizerId;

    /** 活动标题（用于拼通知文案） */
    private String activityTitle;

    /** 报名者ID（谁报名的，暂不展示，留作扩展：如"xxx 报名了你的活动"） */
    private Long actorId;

    /** 报名后的最新报名人数 */
    private Integer currentParticipants;
}
