package com.campushub.campusnotificationservice.domain.dto;

import lombok.Data;

/**
 * 通知列表查询参数 DTO
 *
 * 对应前端 NotificationQuery：分页 + 已读状态筛选（可选）。
 * read 用包装类型 Boolean：null 表示"不筛选"，true/false 才是筛选条件——这就是不用基本类型 boolean 的原因
 *
 * @author CampusHub
 */
@Data
public class NotificationQueryDTO {

    /** 已读状态筛选：null 全部 / true 已读 / false 未读 */
    private Boolean read;

    /** 页码，默认第 1 页 */
    private long pageNum = 1;

    /** 每页条数，默认 10 */
    private long pageSize = 10;
}