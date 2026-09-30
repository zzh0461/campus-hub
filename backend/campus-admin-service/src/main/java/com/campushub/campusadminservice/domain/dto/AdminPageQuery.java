package com.campushub.campusadminservice.domain.dto;

import lombok.Data;

/**
 * 后台通用列表查询参数（admin 侧）
 *
 * 后台四个管理列表（公告/商品/活动/失物）的查询串结构完全一致：
 * 分页 + 关键词（各服务自行决定命中哪些列），故共用这一个查询对象，
 * admin 收到后再透传给对应服务的 internal 管理接口。
 *
 * @author CampusHub
 */
@Data
public class AdminPageQuery {

    /** 关键词：null 或空串不筛选 */
    private String keyword;

    /** 页码，默认第 1 页 */
    private long pageNum = 1;

    /** 每页条数，默认 10 */
    private long pageSize = 10;
}
