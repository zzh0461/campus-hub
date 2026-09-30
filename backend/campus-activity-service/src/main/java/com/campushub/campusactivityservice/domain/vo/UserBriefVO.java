package com.campushub.campusactivityservice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户简要信息 VO（activity-service 内部契约副本）
 *
 * 对应 user-service 的 UserBriefVO（id/昵称/头像），用于把发布者昵称填进 organizer 字段。
 * 微服务间不共享业务 VO，以 JSON 字段名为契约。
 *
 * @author CampusHub
 */
@Data
public class UserBriefVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long id;

    /** 昵称 */
    private String nickname;

    /** 头像 URL */
    private String avatar;
}
