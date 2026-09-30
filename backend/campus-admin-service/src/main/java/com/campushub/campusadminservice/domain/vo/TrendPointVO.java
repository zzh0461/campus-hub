package com.campushub.campusadminservice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 趋势图单点数据（admin 侧契约副本）
 *
 * 对应前端 types/dashboard.ts 的 TrendPoint：x 轴日期 + y 轴数量。
 * 各下游服务统计接口返回的 JSON 按字段名反序列化到本类。
 *
 * @author CampusHub
 */
@Data
public class TrendPointVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 日期标签，格式 MM-dd（如 09-15） */
    private String date;

    /** 当日数量 */
    private long value;
}
