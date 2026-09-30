package com.campushub.campuscontentservice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 失物招领展示对象 VO
 *
 * 对应前端 types/lostFound.ts 的 LostFoundItem 结构。
 * 相比实体：images 由 JSON 字符串解析为 List<String>；
 * 额外补 publisherName/publisherAvatar（跨服务，Feign 调 user-service 填充）
 *
 * @author CampusHub
 */
@Data
public class LostFoundVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long id;

    /** 类型：LOST / FOUND */
    private String type;

    /** 标题 */
    private String title;

    /** 详细描述 */
    private String description;

    /** 地点 */
    private String location;

    /** 联系方式（发布者自行填写的自由文本：电话 / QQ / 微信等） */
    private String contact;

    /**
     * 当前请求者是否有权看到 {@link #contact}。
     *
     * 隐私口径：联系方式仅对已登录用户开放（游客为 false，且 contact 会被置空）。
     * 前端据此渲染"登录后查看联系方式"的引导。
     */
    private Boolean contactVisible;

    /** 图片 URL 列表（实体里是 JSON 字符串，这里已解析） */
    private List<String> images;

    /** 状态：OPEN / RESOLVED */
    private String status;

    /** 发布者用户ID */
    private Long publisherId;

    /** 发布者昵称（Feign 跨服务填充，降级时为 null） */
    private String publisherName;

    /** 发布者头像（Feign 跨服务填充，降级时为 null） */
    private String publisherAvatar;

    /** 发布时间 */
    private LocalDateTime createdAt;
}
