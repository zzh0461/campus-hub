package com.campushub.campusadminservice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 活动侧统计 VO（admin 侧契约副本）
 *
 * 对应 activity-service 的 ActivityStatsVO：活动总数、今日报名、近 14 天报名趋势。
 * 仅供 Dashboard 聚合使用，不直接返回给前端。
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

    /** 近 14 天报名趋势 */
    private List<TrendPointVO> registrationTrend;
}
