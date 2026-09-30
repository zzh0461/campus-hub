package com.campushub.campuscontentservice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户简要信息 VO（content-service 侧"契约副本"）
 *
 * 字段与 user-service 的 UserBriefVO 保持一致：Feign 调用后，user-service 返回的 JSON
 * 会按"字段名"反序列化到本类。微服务之间不共享 VO，各自持有一份契约副本，
 * 从而降低耦合（一个服务改内部实现，不会连累另一个服务编译）。
 *
 * 与 market-service 侧那份 UserBriefVO 是"同结构、不同包"的两份副本，互不依赖。
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
