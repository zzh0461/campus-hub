package com.campushub.campusactivityservice.domain.dto;

import lombok.Data;

/**
 * 我的活动分页查询 DTO
 *
 * 对应前端 getMyActivities(PageParams)：只有分页参数，无筛选条件
 *
 * @author CampusHub
 */
@Data
public class MyActivityQueryDTO {

    /** 页码，默认第 1 页 */
    private long pageNum = 1;

    /** 每页条数，默认 10（与前端 MyActivities.vue 的初始值一致） */
    private long pageSize = 10;
}
