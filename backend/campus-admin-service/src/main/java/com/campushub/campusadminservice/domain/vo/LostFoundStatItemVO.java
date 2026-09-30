package com.campushub.campusadminservice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 失物招领单项统计（admin 侧契约副本）
 *
 * 对应前端 types/dashboard.ts 的 LostFoundStatItem：LOST / FOUND 各一条。
 *
 * @author CampusHub
 */
@Data
public class LostFoundStatItemVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 类型：LOST 失物 / FOUND 招领 */
    private String type;

    /** 该类型的记录数 */
    private long count;
}
