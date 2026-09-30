package com.campushub.campusadminservice.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 公告（新建/编辑）保存参数（admin 侧契约副本）
 *
 * 承接前端 createAnnouncement / updateAnnouncement 发来的请求体，
 * 对应 AnnouncementPublishParams：标题/内容/分类/发布状态。
 * 作者不接收前端传，由 content-service 统一署名"平台管理员"。
 *
 * @author CampusHub
 */
@Data
public class AnnouncementSaveDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 公告标题 */
    @NotBlank(message = "公告标题不能为空")
    private String title;

    /** 公告内容 */
    @NotBlank(message = "公告内容不能为空")
    private String content;

    /** 分类 */
    @NotBlank(message = "公告分类不能为空")
    private String category;

    /** 是否发布：false 存草稿，true 立即对用户可见 */
    @NotNull(message = "发布状态不能为空")
    private Boolean published;
}
