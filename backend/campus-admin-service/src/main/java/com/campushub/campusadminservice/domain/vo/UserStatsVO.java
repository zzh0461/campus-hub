package com.campushub.campusadminservice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 用户侧统计 VO（admin 侧契约副本）
 *
 * 对应 user-service 的 UserStatsVO：用户总数、今日新增、近 14 天注册趋势。
 * 仅供 Dashboard 聚合使用，不直接返回给前端。
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

    /** 近 14 天用户增长趋势 */
    private List<TrendPointVO> userGrowth;
}
