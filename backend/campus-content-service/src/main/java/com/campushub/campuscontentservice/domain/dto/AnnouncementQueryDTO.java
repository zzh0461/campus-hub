package com.campushub.campuscontentservice.domain.dto;

import lombok.Data;

/**
 * 公告列表查询参数 DTO
 *
 * 对应前端 AnnouncementQuery：分页 + 关键词/分类筛选（全部可选）。
 * 前端"全部分类"时不发送 category，后端收到 null 即不筛选
 *
 * @author CampusHub
 */
@Data
public class AnnouncementQueryDTO {

    /** 关键词：标题/内容模糊匹配，null 或空串不筛选 */
    private String keyword;

    /** 分类筛选，null 或空串查全部 */
    private String category;

    /** 页码，默认第 1 页 */
    private long pageNum = 1;

    /** 每页条数，默认 10 */
    private long pageSize = 10;
}
