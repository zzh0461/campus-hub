package com.campushub.campusmarketservice.domain.dto;

import lombok.Data;

/**
 * 后台商品列表查询参数 DTO
 *
 * 供 admin-service 通过 internal 接口分页拉取商品，对应前端 getAdminProducts：
 * 分页 + 关键词（标题模糊匹配，可选）。
 * 与用户侧 ProductQueryDTO 的区别：后台看全部状态（在售/下架/已售出），不接受价格/排序筛选。
 *
 * @author CampusHub
 */
@Data
public class AdminProductQueryDTO {

    /** 关键词：标题模糊匹配，null 或空串不筛选 */
    private String keyword;

    /** 页码，默认第 1 页 */
    private long pageNum = 1;

    /** 每页条数，默认 10 */
    private long pageSize = 10;
}
