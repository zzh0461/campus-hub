package com.campushub.campususerservice.domain.dto;

import lombok.Data;

/**
 * 后台用户列表查询参数 DTO
 *
 * 供 admin-service 通过 internal 接口分页拉取用户，对应前端 AdminUserQuery：
 * 分页 + 关键词（用户名/昵称/手机号模糊匹配，可选）。
 *
 * @author CampusHub
 */
@Data
public class AdminUserQueryDTO {

    /** 关键词：用户名/昵称/手机号模糊匹配，null 或空串不筛选 */
    private String keyword;

    /** 页码，默认第 1 页 */
    private long pageNum = 1;

    /** 每页条数，默认 10 */
    private long pageSize = 10;
}
