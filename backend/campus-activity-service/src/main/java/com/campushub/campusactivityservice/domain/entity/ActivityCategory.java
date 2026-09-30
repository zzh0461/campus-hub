package com.campushub.campusactivityservice.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 活动分类实体，对应 campus_activity_category 表
 *
 * @author CampusHub
 */
@Data
@TableName("campus_activity_category")
public class ActivityCategory implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 分类ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类名称 */
    private String name;

    /** 排序值（升序展示） */
    private Integer sortOrder;
}
