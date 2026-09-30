package com.campushub.campususerservice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户公开信息 VO（"他人可见的名片"）
 *
 * 用于商品详情页等场景展示卖家介绍：任何登录用户都可以查看其他用户的这份公开资料。
 * 与 UserVO 的区别：刻意不返回 username / role / status 等敏感或无关字段。
 *
 * <h3>联系方式可见性</h3>
 * 手机号属于联系方式，只做登录门控（注册时手机号必填，登录用户之间互相可见）：
 * <ul>
 *   <li>{@link #phoneVisible} = false 时 {@link #phone} 一定是空串，前端不应展示任何号码；</li>
 *   <li>已登录时 {@link #phone} 为完整明文，前端直接展示 + 复制。</li>
 * </ul>
 *
 * @author CampusHub
 */
@Data
public class UserPublicVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long id;

    /** 昵称 */
    private String nickname;

    /** 头像 URL */
    private String avatar;

    /** 个人简介 */
    private String bio;

    /** 注册时间 */
    private LocalDateTime createdAt;

    /**
     * 联系方式（卖家资料中的手机号，明文返回）。
     *
     * 只有在 {@link #phoneVisible} 为 true 时才有值；未填写或未解锁时为空串。
     */
    private String phone;

    /**
     * 当前请求者是否有权看到 {@link #phone}。
     *
     * true = 已登录；false = 未登录（此时 {@link #phone} 为空串）。
     */
    private Boolean phoneVisible;

    /**
     * 未登录时的引导文案（后端给口径，前端直接展示）。
     *
     * 已登录时为空串。
     */
    private String contactHint;
}
