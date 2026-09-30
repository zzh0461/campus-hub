package com.campushub.campususerservice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 用户侧后台统计 VO（供 admin-service 的 Dashboard 聚合）
 *
 * 用户服务只统计自己领域内的数据：用户总数、今日新增、近 14 天注册趋势。
 *
 * @author CampusHub
 */
@Data
public class UserStatsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户总数 */
    private long userTotal;

    /** 今日新增用户数 */
    private long todayNewUsers;

    /** 近 14 天用户增长趋势（日期轴连续，无新增的日期补 0） */
    private List<TrendPointVO> userGrowth;
}
