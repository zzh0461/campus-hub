package com.campushub.campusadminservice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台失物招领信息 VO（admin 侧契约副本）
 *
 * 字段与 content-service 的 LostFoundVO、前端 types/lostFound.ts 的 LostFoundItem 对齐。
 *
 * @author CampusHub
 */
@Data
public class LostFoundVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long id;

    /** 类型：LOST 失物 / FOUND 招领 */
    private String type;

    /** 标题 */
    private String title;

    /** 描述 */
    private String description;

    /** 地点 */
    private String location;

    /** 联系方式 */
    private String contact;

    /** 图片URL列表 */
    private List<String> images;

    /** 状态：OPEN / RESOLVED */
    private String status;

    /** 发布者用户ID */
    private Long publisherId;

    /** 发布者昵称 */
    private String publisherName;

    /** 发布者头像 URL */
    private String publisherAvatar;

    /** 发布时间 */
    private LocalDateTime createdAt;
}
