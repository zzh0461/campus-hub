package com.campushub.campusadminservice.controller;

import com.campushub.campusadminservice.client.ActivityAdminClient;
import com.campushub.campusadminservice.client.LostFoundAdminClient;
import com.campushub.campusadminservice.client.ProductAdminClient;
import com.campushub.campusadminservice.client.UserAdminClient;
import com.campushub.campusadminservice.domain.vo.ActivityStatsVO;
import com.campushub.campusadminservice.domain.vo.ContentStatsVO;
import com.campushub.campusadminservice.domain.vo.DashboardStatsVO;
import com.campushub.campusadminservice.domain.vo.DashboardVO;
import com.campushub.campusadminservice.domain.vo.MarketStatsVO;
import com.campushub.campusadminservice.domain.vo.UserStatsVO;
import com.campushub.common.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台数据概览控制器（Dashboard 聚合）
 *
 * 路径在 /api/admin/dashboard 下，网关强制 JWT 鉴权，
 * 且 AdminAuthInterceptor 已保证到这里的一定是 ADMIN 角色，故方法内无需再校验身份。
 *
 * admin 是聚合门面：Dashboard 的每一个数字都不归它管，
 * 分别向 user / market / activity / content 四个服务的 stats 内部接口拉取，
 * 本地只做拼装（四张总数卡 + 三组趋势 + 失物分类统计），不碰任何数据库。
 *
 * @author CampusHub
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final UserAdminClient userAdminClient;
    private final ProductAdminClient productAdminClient;
    private final ActivityAdminClient activityAdminClient;
    private final LostFoundAdminClient lostFoundAdminClient;

    /**
     * 数据概览：统计卡 + 用户增长/商品发布/活动报名三组趋势 + 失物招领统计
     *
     * @return 前端 Dashboard 页所需的聚合数据
     */
    @GetMapping
    public R<DashboardVO> dashboard() {
        // 1.分别拉取四个领域的统计（不设 fallback：某个服务挂了就该报错，
        //   而不是拿一份缺数字的假概览误导管理员）
        UserStatsVO userStats = userAdminClient.stats();
        MarketStatsVO marketStats = productAdminClient.stats();
        ActivityStatsVO activityStats = activityAdminClient.stats();
        ContentStatsVO contentStats = lostFoundAdminClient.stats();

        // 2.拼装统计卡：每个服务的"总数/今日新增"各取所需
        DashboardStatsVO stats = new DashboardStatsVO();
        stats.setUserTotal(userStats.getUserTotal());
        stats.setProductTotal(marketStats.getProductTotal());
        stats.setActivityTotal(activityStats.getActivityTotal());
        stats.setLostFoundTotal(contentStats.getLostFoundTotal());
        stats.setTodayNewUsers(userStats.getTodayNewUsers());
        stats.setTodayNewProducts(marketStats.getTodayNewProducts());
        stats.setTodayRegistrations(activityStats.getTodayRegistrations());

        // 3.拼装趋势与分类统计
        DashboardVO vo = new DashboardVO();
        vo.setStats(stats);
        vo.setUserGrowth(userStats.getUserGrowth());
        vo.setProductTrend(marketStats.getProductTrend());
        vo.setRegistrationTrend(activityStats.getRegistrationTrend());
        vo.setLostFoundStats(contentStats.getLostFoundStats());
        return R.success(vo);
    }
}
