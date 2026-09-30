package com.campushub.campususerservice.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户简要信息 VO（跨服务"对外名片"）
 *
 * 仅暴露其它服务展示所需的最小字段（如 market-service 显示卖家昵称、头像），
 * 刻意不含 username / phone / role 等敏感或无关字段，避免过度暴露。
 * 保留无参构造器，供 Feign 反序列化使用。
 *
 * @author CampusHub
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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