package com.campushub.campusadminservice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 后台数据概览聚合 VO（admin 侧契约副本）
 *
 * 对应前端 types/dashboard.ts 的 DashboardData，即 GET /api/admin/dashboard 的 data：
 * stats 统计卡 + 三组趋势折线（用户增长/商品发布/活动报名）+ 失物招领分类统计。
 * 数据全部来自各下游服务的 /internal/**\/stats 接口，本服务只做聚合拼装，不碰任何数据库。
 *
 * @author CampusHub
 */
@Data
public class DashboardVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 统计卡片数据 */
    private DashboardStatsVO stats;

    /** 用户增长趋势（近 14 天） */
    private List<TrendPointVO> userGrowth;

    /** 商品发布趋势（近 14 天） */
    private List<TrendPointVO> productTrend;

    /** 活动报名趋势（近 14 天） */
    private List<TrendPointVO> registrationTrend;

    /** 失物招领分类统计（LOST / FOUND 两条） */
    private List<LostFoundStatItemVO> lostFoundStats;
}
