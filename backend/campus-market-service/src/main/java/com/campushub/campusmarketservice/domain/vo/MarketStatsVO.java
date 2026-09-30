package com.campushub.campusmarketservice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 市场侧后台统计 VO（供 admin-service 的 Dashboard 聚合）
 *
 * 市场服务只统计自己领域内的数据：商品总数、今日新增、近 14 天发布趋势。
 *
 * @author CampusHub
 */
@Data
public class MarketStatsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 商品总数（含全部状态） */
    private long productTotal;

    /** 今日新增商品数 */
    private long todayNewProducts;

    /** 近 14 天商品发布趋势（日期轴连续，无发布的日期补 0） */
    private List<TrendPointVO> productTrend;
}
