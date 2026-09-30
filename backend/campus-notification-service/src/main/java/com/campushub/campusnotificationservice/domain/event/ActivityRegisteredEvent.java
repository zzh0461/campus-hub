package com.campushub.campusnotificationservice.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 活动被报名事件（MQ 消息体，JSON 序列化）——activity-service 侧同名类的副本
 *
 * 两边靠 JSON 字段名对齐契约，各自持有本类副本，不通过 common 模块共享
 * （服务自治，避免公共模块膨胀）。字段顺序/类型需与 activity 侧保持一致
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

    /** 报名者ID（谁报名的，暂不展示，留作扩展） */
    private Long actorId;

    /** 报名后的最新报名人数 */
    private Integer currentParticipants;
}
