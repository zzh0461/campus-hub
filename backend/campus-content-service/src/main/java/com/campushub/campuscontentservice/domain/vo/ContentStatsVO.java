package com.campushub.campuscontentservice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 内容服务后台统计 VO（供 admin-service 的 Dashboard 聚合）
 *
 * 内容服务只负责失物招领相关的统计：记录总数 + 失物/招领分类计数。
 * 公告暂无趋势图需求，故不在此统计。
 *
 * @author CampusHub
 */
@Data
public class ContentStatsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 失物招领记录总数 */
    private long lostFoundTotal;

    /** 失物/招领分类计数（两条：LOST、FOUND） */
    private List<LostFoundStatItemVO> lostFoundStats;
}
