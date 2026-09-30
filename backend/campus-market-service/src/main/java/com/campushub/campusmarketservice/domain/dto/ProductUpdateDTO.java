package com.campushub.campusmarketservice.domain.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 商品更新传输对象
 *
 * 继承 ProductPublishDTO，复用标题/分类/价格/描述/图片的全部校验规则；
 * 额外允许可选的 status：编辑内容时不传（保持原状态），上下架时传（切换状态）。
 * 同样不含 sellerId 等受控字段——归属由后端按登录用户校验，杜绝越权改他人商品。
 *
 * @author CampusHub
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductUpdateDTO extends ProductPublishDTO {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 状态：ON_SALE / OFF_SHELF / SOLD；可选（@Pattern 对 null 放行，编辑内容时不传即可） */
    @Pattern(regexp = "ON_SALE|OFF_SHELF|SOLD", message = "商品状态非法")
    private String status;
}