package com.campushub.campusadminservice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Dashboard 概览统计卡片 VO（admin 侧契约副本）
 *
 * 对应前端 types/dashboard.ts 的 DashboardStats：四张总数卡 + 三张今日新增卡。
 * 字段名必须与前端契约完全一致，前端按 userTotal/productTotal/... 直接取值渲染。
 *
 * @author CampusHub
 */
@Data
public class DashboardStatsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户总数 */
    private long userTotal;

    /** 商品总数 */
    private long productTotal;

    /** 活动总数 */
    private long activityTotal;

    /** 失物招领记录总数 */
    private long lostFoundTotal;

    /** 今日新增用户数 */
    private long todayNewUsers;

    /** 今日新增商品数 */
    private long todayNewProducts;

    /** 今日活动报名数 */
    private long todayRegistrations;
}
