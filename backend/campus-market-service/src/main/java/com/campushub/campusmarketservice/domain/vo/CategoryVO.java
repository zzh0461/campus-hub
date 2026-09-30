package com.campushub.campusmarketservice.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 商品分类展示对象 VO
 *
 * 对应前端 types/market.ts 的 ProductCategory 结构 { id, name }。
 * 只暴露下拉框需要的字段，不含 sort_order 等内部字段。
 *
 * @author CampusHub
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 分类ID */
    private Long id;

    /** 分类名称 */
    private String name;
}