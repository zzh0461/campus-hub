package com.campushub.campusadminservice.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动（新建/编辑）保存参数（admin 侧契约副本）
 *
 * 承接前端 createAdminActivity / updateAdminActivity 发来的请求体，
 * 对应 ActivityPublishParams：标题/介绍/分类/地点/起止时间/名额/封面/主办方。
 * 状态由 activity-service 按起止时间推算，不接受前端传。
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

    /** 主办方名称，可选：未填时 activity-service 退回"平台管理员"默认署名 */
    private String organizer;
}
