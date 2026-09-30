package com.campushub.campusmarketservice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 /**
 * 商品展示对象 VO
 *
 * 对应前端 types/market.ts 的 Product 结构。与 Product 实体的区别：
 * 1. images 由数据库的 JSON 字符串解析为 List<String>，前端可直接遍历；
 * 2. 增加 favorite 标记（收藏列表中恒为 true）；
 * 3. 关联填充展示字段：categoryName（本地分类表）、sellerName/sellerAvatar（跨服务调 user-service）；
 * 4. 只暴露前端展示所需字段。
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

    /** 分类名称（展示用，由分类表关联填充） */
    private String categoryName;

    /** 售价 */
    private BigDecimal price;

    /** 原价/参考价 */
    private BigDecimal originalPrice;

    /** 商品描述 */
    private String description;

    /** 图片URL列表（已由 JSON 字符串解析为数组） */
    private List<String> images;

    /** 卖家用户ID */
    private Long sellerId;

    /** 卖家昵称（展示用，跨服务从 user-service 填充） */
    private String sellerName;

    /** 卖家头像 URL（展示用，跨服务从 user-service 填充） */
    private String sellerAvatar;

    /** 状态：ON_SALE / OFF_SHELF / SOLD */
    private String status;

    /** 收藏数 */
    private Integer favoriteCount;

    /** 浏览量 */
    private Integer viewCount;

    /** 当前用户是否已收藏（收藏列表中恒为 true） */
    private Boolean favorite;

    /** 发布时间 */
    private LocalDateTime createdAt;
}