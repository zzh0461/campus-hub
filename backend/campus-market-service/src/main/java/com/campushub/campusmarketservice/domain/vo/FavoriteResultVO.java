package com.campushub.campusmarketservice.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 收藏操作结果 VO
 *
 * 对应前端契约 { favorite: boolean }：true=已收藏，false=已取消收藏。
 *
 * @author CampusHub
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 操作后的收藏状态 */
    private Boolean favorite;
}