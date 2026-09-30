package com.campushub.campusactivityservice.domain.dto;

import lombok.Data;

/**
 * 活动列表查询参数 DTO
 *
 * 对应前端 ActivityQuery：分页 + 关键词/分类/状态筛选（全部可选）。
 * 前端"全部状态"时不发送 status 参数（filters.status || undefined），后端收到 null 即不筛选
 *
 * @author CampusHub
 */
@Data
public class ActivityQueryDTO {

    /** 关键词：标题/介绍模糊匹配，null 或空串不筛选 */
    private String keyword;

    /** 分类ID，null 不筛选 */
    private Long categoryId;

    /** 状态筛选：UPCOMING / ONGOING / FINISHED，null 或空串查全部 */
    private String status;

    /** 页码，默认第 1 页 */
    private long pageNum = 1;

    /** 每页条数，默认 12 */
    private long pageSize = 12;
}
