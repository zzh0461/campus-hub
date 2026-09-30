package com.campushub.campusmarketservice.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类
 * 对应数据库表：campus_product
 *
 * @author CampusHub
 */
@Data
@TableName("campus_product")
public class Product implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 商品ID，数据库自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品标题 */
    private String title;

    /** 分类ID */
    @TableField("category_id")
    private Long categoryId;

    /** 售价（金额用 BigDecimal，绝不用 double，避免精度丢失） */
    private BigDecimal price;

    /** 原价/参考价 */
    @TableField("original_price")
    private BigDecimal originalPrice;

    /** 商品描述 */
    private String description;

    /** 图片URL数组：数据库存 JSON 字符串（如 ["url1","url2"]），转 VO 时再解析成 List */
    private String images;

    /** 卖家用户ID */
    @TableField("seller_id")
    private Long sellerId;

    /** 状态：ON_SALE / OFF_SHELF / SOLD */
    private String status;

    /** 收藏数（冗余字段，收藏/取消时同步） */
    @TableField("favorite_count")
    private Integer favoriteCount;

    /** 浏览量（详情接口 +1） */
    @TableField("view_count")
    private Integer viewCount;

    /** 发布时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间，数据库自动维护 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}