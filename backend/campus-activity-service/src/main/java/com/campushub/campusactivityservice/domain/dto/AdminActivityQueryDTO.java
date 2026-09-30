package com.campushub.campusactivityservice.domain.dto;

import lombok.Data;

/**
 * 后台活动列表查询参数 DTO
 *
 * 供 admin-service 通过 internal 接口分页拉取活动，对应前端 getAdminActivities：
 * 分页 + 关键词（标题/介绍模糊匹配，可选）。
 *
 * @author CampusHub
 */
@Data
public class AdminActivityQueryDTO {

    /** 关键词：标题/介绍模糊匹配，null 或空串不筛选 */
    private String keyword;

    /** 页码，默认第 1 页 */
    private long pageNum = 1;

    /** 每页条数，默认 10 */
    private long pageSize = 10;
}
