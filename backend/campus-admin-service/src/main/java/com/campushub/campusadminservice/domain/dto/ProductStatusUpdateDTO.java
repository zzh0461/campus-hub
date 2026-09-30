package com.campushub.campusadminservice.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 商品状态更新传输对象（admin 侧契约副本）
 *
 * 承接前端 updateAdminProductStatus 发来的 { status } 请求体，再原样透传给 market-service。
 * 两侧各有自己的 DTO 类，以 JSON 字段名(status)为契约，互不依赖对方的类。
 *
 * @author CampusHub
 */
@Data
public class ProductStatusUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 目标状态：ON_SALE / OFF_SHELF / SOLD，具体合法性由 market-service 校验 */
    @NotBlank(message = "状态不能为空")
    private String status;
}
