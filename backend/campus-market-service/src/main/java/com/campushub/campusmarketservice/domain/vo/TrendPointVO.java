package com.campushub.campusmarketservice.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 趋势图单点数据（market-service 内部）
 *
 * 对应前端 types/dashboard.ts 的 TrendPoint：x 轴日期 + y 轴数量。
 * 日期轴连续（14 天，无数据的日期补 0），前端 ECharts 直接按序渲染。
 *
 * @author CampusHub
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendPointVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 日期标签，格式 MM-dd（如 09-15） */
    private String date;

    /** 当日数量 */
    private long value;
}
