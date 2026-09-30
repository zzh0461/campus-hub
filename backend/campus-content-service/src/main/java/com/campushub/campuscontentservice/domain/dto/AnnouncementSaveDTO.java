package com.campushub.campuscontentservice.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 公告新建/编辑传输对象（后台管理用）
 *
 * 对应前端 AnnouncementPublishParams：title/content/category/published。
 * 刻意不含 author——后台新建的公告统一署名"平台管理员"，由 Service 填充，不接受前端传，
 * 避免伪造发布单位。
 *
 * @author CampusHub
 */
@Data
public class AnnouncementSaveDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 公告标题 */
    @NotBlank(message = "公告标题不能为空")
    @Size(max = 60, message = "公告标题最多 60 个字")
    private String title;

    /** 公告内容（长文本） */
    @NotBlank(message = "公告内容不能为空")
    private String content;

    /** 分类：通知公告/教务信息/后勤服务/校园活动 */
    @NotBlank(message = "公告分类不能为空")
    private String category;

    /** 是否立即发布：null 视为草稿（不发布） */
    private Boolean published;
}
