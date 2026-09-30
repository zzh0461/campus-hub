package com.campushub.campususerservice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品展示对象 VO（user-service 侧"契约副本"）
 *
 * 字段必须与 market-service 的 ProductVO 保持一致：Feign 调用后，market 返回的 JSON
 * 会按"字段名"反序列化到本类。微服务之间不共享 VO，各自持有一份契约副本，
 * 从而降低耦合（一个服务改内部实现，不会连累另一个服务编译）。
 *
 * <p>注意：这里是"契约副本"，**少写一个字段就会静默丢数据**——
 * Jackson 默认忽略 JSON 里多出来的字段，不报错。
 * 此前缺少 categoryName / sellerName / sellerAvatar，导致"我的收藏"列表
 * 拿不到卖家昵称与头像（前端只能逐个回查商品详情兜底）。
 * 新增 market 侧 ProductVO 字段时，务必同步这一份。
 *
 * @author CampusHub
 */
@Data
public class ProductVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private Long categoryId;
    /** 分类名称（展示用，market-service 关联分类表填充） */
    private String categoryName;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String description;
    private List<String> images;
    private Long sellerId;
    /** 卖家昵称（展示用，market-service 跨服务从 user-service 填充） */
    private String sellerName;
    /** 卖家头像 URL（展示用，market-service 跨服务从 user-service 填充） */
    private String sellerAvatar;
    private String status;
    private Integer favoriteCount;
    private Integer viewCount;
    private Boolean favorite;
    private LocalDateTime createdAt;
}