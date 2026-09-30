package com.campushub.campuscontentservice.domain.dto;

import lombok.Data;

/**
 * 失物招领列表查询参数 DTO
 *
 * 对应前端 LostFoundQuery：分页 + 关键词/类型筛选（全部可选）。
 * 前端"全部类型"时不发送 type，后端收到 null 即不筛选
 *
 * @author CampusHub
 */
@Data
public class LostFoundQueryDTO {

    /** 关键词：标题/描述模糊匹配，null 或空串不筛选 */
    private String keyword;

    /** 类型筛选：LOST / FOUND，null 或空串查全部 */
    private String type;

    /** 页码，默认第 1 页 */
    private long pageNum = 1;

    /** 每页条数，默认 10 */
    private long pageSize = 10;
}
