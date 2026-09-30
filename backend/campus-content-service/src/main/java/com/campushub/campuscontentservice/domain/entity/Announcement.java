package com.campushub.campuscontentservice.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 校园公告实体，对应 campus_announcement 表
 *
 * @author CampusHub
 */
@Data
@TableName("campus_announcement")
public class Announcement implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 公告ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 公告标题 */
    private String title;

    /** 公告内容（长文本） */
    private String content;

    /** 分类：通知公告/教务信息/后勤服务/校园活动 */
    private String category;

    /** 发布单位（冗余文本，如"图书馆""教务处"） */
    private String author;

    /** 是否已发布：TINYINT(1) 映射 Boolean，用户侧只展示 true 的 */
    private Boolean published;

    /** 浏览量（view_count 靠下划线转驼峰自动映射），详情访问时原子 +1 */
    private Integer viewCount;

    /** 发布时间（数据库默认 CURRENT_TIMESTAMP） */
    private LocalDateTime createdAt;

    /** 更新时间（数据库 ON UPDATE CURRENT_TIMESTAMP 自动维护） */
    private LocalDateTime updatedAt;
}
