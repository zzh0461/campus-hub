package com.campushub.campususerservice.domain.dto;

import lombok.Data;

/**
 * 按天分组的计数投影 DTO（user-service 内部）
 *
 * 承接 UserMapper 自定义 SQL 的 GROUP BY 查询结果：
 * 某一天（MM-dd）新增了多少个用户。只作为查询中间产物，
 * 由 service 补齐"没有新增的日期补 0"后转成 TrendPointVO。
 *
 * @author CampusHub
 */
@Data
public class DailyCountDTO {

    /** 日期标签，格式 MM-dd（如 09-15） */
    private String statDate;

    /** 该日期的记录数 */
    private long cnt;
}
