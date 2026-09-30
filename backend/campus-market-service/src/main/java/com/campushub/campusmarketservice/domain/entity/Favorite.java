package com.campushub.campusmarketservice.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品收藏实体类
 * 对应数据库表：campus_favorite
 *
 * 这是一张"关系表"：只记录"哪个用户收藏了哪个商品"，
 * 不含商品详情，详情需再查 campus_product。
 *
 * @author CampusHub
 */
@Data
@TableName("campus_favorite")
public class Favorite implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 商品ID */
    @TableField("product_id")
    private Long productId;

    /** 收藏时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;
}