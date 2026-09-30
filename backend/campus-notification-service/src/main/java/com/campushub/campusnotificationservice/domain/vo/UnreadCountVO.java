package com.campushub.campusnotificationservice.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 未读通知数量 VO
 *
 * 对应前端契约 { count: number }：顶栏铃铛小红点的数字来源。
 *
 * @author CampusHub
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnreadCountVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 当前用户未读通知数量 */
    private Long count;
}
