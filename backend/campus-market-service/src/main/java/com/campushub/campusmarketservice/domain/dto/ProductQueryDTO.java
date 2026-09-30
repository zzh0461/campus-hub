package com.campushub.campusmarketservice.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品列表查询参数 DTO
 *
 * 对应前端 ProductQuery：分页 + 关键词/分类/价格区间/排序（均可选）。
 * Spring MVC 会自动把 URL 上的 ?pageNum=1&sort=latest... 绑定到本对象同名字段。
 *
 * @author CampusHub
 */
@Data
public class ProductQueryDTO {

    /** 关键词（模糊匹配标题），可选 */
    private String keyword;

    /** 分类ID，可选 */
    private Long categoryId;

    /** 最低价，可选 */
    private BigDecimal minPrice;

    /** 最高价，可选 */
    private BigDecimal maxPrice;

    /** 排序方式：latest(默认) / priceAsc / priceDesc */
    private String sort;

    /** 页码，默认第 1 页 */
    private long pageNum = 1;

    /** 每页条数，默认 12 */
    private long pageSize = 12;
}