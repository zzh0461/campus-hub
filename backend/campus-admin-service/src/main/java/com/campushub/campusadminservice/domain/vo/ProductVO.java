package com.campushub.campusadminservice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台商品信息 VO（admin 侧契约副本）
 *
 * 字段与 market-service 的 ProductVO、前端 types/market.ts 的 Product 对齐。
 * Feign 收到 market-service 返回的 JSON 后按字段名反序列化到本类，再套 R 返回给前端后台。
 *
 * @author CampusHub
 */
@Data
public class ProductVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 商品ID */
    private Long id;

    /** 商品标题 */
    private String title;

    /** 分类ID */
    private Long categoryId;

    /** 分类名称 */
    private String categoryName;

    /** 售价 */
    private BigDecimal price;

    /** 原价/参考价 */
    private BigDecimal originalPrice;

    /** 商品描述 */
    private String description;

    /** 图片URL列表 */
    private List<String> images;

    /** 卖家用户ID */
    private Long sellerId;

    /** 卖家昵称 */
    private String sellerName;

    /** 卖家头像 URL */
    private String sellerAvatar;

    /** 状态：ON_SALE / OFF_SHELF / SOLD */
    private String status;

    /** 收藏数 */
    private Integer favoriteCount;

    /** 浏览量 */
    private Integer viewCount;

    /** 当前登录用户是否已收藏（后台列表恒为 false） */
    private Boolean favorite;

    /** 发布时间 */
    private LocalDateTime createdAt;
}
