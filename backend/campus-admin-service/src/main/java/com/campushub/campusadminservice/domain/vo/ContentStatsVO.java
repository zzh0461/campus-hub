package com.campushub.campusadminservice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 内容侧统计 VO（admin 侧契约副本）
 *
 * 对应 content-service 的 ContentStatsVO：失物招领总数 + 失物/招领分类计数。
 * 仅供 Dashboard 聚合使用，不直接返回给前端。
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
