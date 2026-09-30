package com.campushub.campusmarketservice.domain.dto;

import lombok.Data;

/**
 * 我的商品查询参数 DTO
 *
 * 对应前端 MyProducts 页：分页 + 状态筛选（可选）。
 * 卖家ID 不在这里——由后端从登录上下文注入，杜绝越权查他人商品。
 *
 * @author CampusHub
 */
@Data
public class MyProductQueryDTO {

    /** 状态筛选：ON_SALE / OFF_SHELF / SOLD；为空表示"全部"（前端"全部"页签不传此参数） */
    private String status;

    /** 页码，默认第 1 页 */
    private long pageNum = 1;

    /** 每页条数，默认 10（与前端 MyProducts 的 pageSize 对齐） */
    private long pageSize = 10;
}