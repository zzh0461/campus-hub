package com.campushub.campusmarketservice.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品发布传输对象
 *
 * 字段与前端 types/market.ts 的 ProductPublishParams 契约对齐。
 * 只包含允许卖家自行填写的"内容"字段；sellerId / status / 计数 / 时间等受控字段
 * 一律不接受前端传入，由后端从登录上下文与数据库默认值填充，杜绝越权与伪造。
 *
 * @author CampusHub
 */
@Data
public class ProductPublishDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 商品标题 */
    @NotBlank(message = "商品标题不能为空")
    @Size(max = 60, message = "商品标题不超过 60 字")
    private String title;

    /** 分类ID */
    @NotNull(message = "请选择商品分类")
    private Long categoryId;

    /** 售价（金额用 BigDecimal，绝不用 double，避免精度丢失） */
    @NotNull(message = "请输入价格")
    @DecimalMin(value = "0.00", message = "价格不能为负数")
    private BigDecimal price;

    /** 商品描述 */
    @NotBlank(message = "商品描述不能为空")
    @Size(min = 10, max = 2000, message = "描述至少 10 个字、不超过 2000 字")
    private String description;

    /** 图片URL列表（可为空，但最多 6 张，与前端 ImageUpload 的 max 对齐） */
    @Size(max = 6, message = "商品图片最多 6 张")
    private List<String> images;
}