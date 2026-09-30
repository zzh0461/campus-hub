package com.campushub.campusmarketservice.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 后台商品状态更新 DTO
 *
 * 承接后台"上架/下架/标记售出"操作发来的 { status } 请求体。
 *
 * @author CampusHub
 */
@Data
public class ProductStatusUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 目标状态：ON_SALE / OFF_SHELF / SOLD，具体合法性由 service 白名单校验 */
    @NotBlank(message = "状态不能为空")
    private String status;
}
