package com.campushub.campusactivityservice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 活动侧后台统计 VO（供 admin-service 的 Dashboard 聚合）
 *
 * 活动服务只统计自己领域内的数据：活动总数、今日报名数、近 14 天报名趋势。
 *
 * @author CampusHub
 */
@Data
public class ActivityStatsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 活动总数 */
    private long activityTotal;

    /** 今日活动报名数 */
    private long todayRegistrations;

    /** 近 14 天报名趋势（日期轴连续，无报名的日期补 0） */
    private List<TrendPointVO> registrationTrend;
}
