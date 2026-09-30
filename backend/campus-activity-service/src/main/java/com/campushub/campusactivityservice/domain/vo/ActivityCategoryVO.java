package com.campushub.campusactivityservice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 活动分类 VO
 *
 * 对应前端契约 ActivityCategory { id, name }：
 * sortOrder 只用于排序，不暴露给前端（VO 只给展示所需字段）
 *
 * @author CampusHub
 */
@Data
public class ActivityCategoryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 分类ID */
    private Long id;

    /** 分类名称 */
    private String name;
}
