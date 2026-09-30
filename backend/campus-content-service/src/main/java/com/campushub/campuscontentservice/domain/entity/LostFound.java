package com.campushub.campuscontentservice.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 失物招领实体，对应 campus_lost_found 表
 *
 * @author CampusHub
 */
@Data
@TableName("campus_lost_found")
public class LostFound implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 类型：LOST 失物 / FOUND 招领 */
    private String type;

    /** 标题 */
    private String title;

    /** 详细描述 */
    private String description;

    /** 地点 */
    private String location;

    /** 联系方式（可空） */
    private String contact;

    /**
     * 图片 URL 数组：数据库是 JSON 列，实体这里用 String 承接原始 JSON 文本
     * （如 ["url1","url2"]），转 VO 时由 LostFoundConverter 解析成 List<String>
     */
    private String images;

    /** 状态：OPEN 进行中 / RESOLVED 已解决 */
    private String status;

    /** 发布者用户ID（昵称/头像不冗余，靠 Feign 调 user-service 填充） */
    private Long publisherId;

    /** 发布时间（数据库默认 CURRENT_TIMESTAMP） */
    private LocalDateTime createdAt;

    /** 更新时间（数据库 ON UPDATE CURRENT_TIMESTAMP 自动维护） */
    private LocalDateTime updatedAt;
}
