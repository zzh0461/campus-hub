package com.campushub.campusadminservice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 市场侧统计 VO（admin 侧契约副本）
 *
 * 对应 market-service 的 MarketStatsVO：商品总数、今日新增、近 14 天发布趋势。
 * 仅供 Dashboard 聚合使用，不直接返回给前端。
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

    /** 近 14 天商品发布趋势 */
    private List<TrendPointVO> productTrend;
}
