package com.campushub.campusmarketservice.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 商品分类实体类
 * 对应数据库表：campus_product_category
 *
 * @author CampusHub
 */
@Data
@TableName("campus_product_category")
public class Category implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 分类ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类名称 */
    @TableField("name")
    private String name;

    /** 排序值（越小越靠前） */
    @TableField("sort_order")
    private Integer sortOrder;
}