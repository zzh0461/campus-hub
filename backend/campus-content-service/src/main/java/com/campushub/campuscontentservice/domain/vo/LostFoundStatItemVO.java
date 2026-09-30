package com.campushub.campuscontentservice.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 失物招领单项统计（类型 + 数量）
 *
 * 供后台 Dashboard 的"失物招领统计"饼图使用：LOST 失物 / FOUND 招领各一条。
 *
 * @author CampusHub
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LostFoundStatItemVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 类型：LOST 失物 / FOUND 招领 */
    private String type;

    /** 该类型的记录数 */
    private long count;
}
