package com.campushub.campusactivityservice.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动（新建/编辑）保存参数 DTO
 *
 * 承接两路来源的同一个请求体结构：
 * 1. 后台 createActivityForAdmin / updateActivityForAdmin——可显式指定主办方；
 * 2. 用户侧 publishActivity / updateMyActivity——organizer 不传，由 Service 用发布者昵称填充。
 * 状态由后端按起止时间推算，不接受前端传。
 *
 * @author CampusHub
 */
@Data
public class ActivitySaveDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 活动标题 */
    @NotBlank(message = "活动标题不能为空")
    private String title;

    /** 活动介绍 */
    private String description;

    /** 分类ID */
    @NotNull(message = "活动分类不能为空")
    private Long categoryId;

    /** 活动地点 */
    @NotBlank(message = "活动地点不能为空")
    private String location;

    /** 开始时间 */
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    /** 结束时间 */
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    /** 人数上限 */
    @NotNull(message = "人数上限不能为空")
    @Min(value = 1, message = "人数上限至少为 1")
    private Integer maxParticipants;

    /** 封面图 URL，可选 */
    private String cover;

    /** 主办方名称：后台可指定；用户发布时不传，由 Service 填充为发布者昵称 */
    private String organizer;
}
