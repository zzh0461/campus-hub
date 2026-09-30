package com.campushub.campuscontentservice.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 失物招领发布传输对象
 *
 * 字段与前端 types/lostFound.ts 的 LostFoundPublishParams 契约对齐。
 * 只包含允许用户自行填写的"内容"字段；publisherId / status / 时间等受控字段
 * 一律不接受前端传入，由后端从登录上下文与数据库默认值填充，杜绝越权与伪造。
 *
 * @author CampusHub
 */
@Data
public class LostFoundPublishDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 类型：LOST 失物 / FOUND 招领 */
    @NotBlank(message = "请选择失物或招领类型")
    private String type;

    /** 标题 */
    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题不超过 100 字")
    private String title;

    /** 详细描述 */
    @Size(max = 2000, message = "描述不超过 2000 字")
    private String description;

    /** 地点 */
    @NotBlank(message = "地点不能为空")
    @Size(max = 100, message = "地点不超过 100 字")
    private String location;

    /** 联系方式（可空） */
    @Size(max = 100, message = "联系方式不超过 100 字")
    private String contact;

    /** 图片URL列表（可为空，最多 6 张，与前端 ImageUpload 的 max 对齐） */
    @Size(max = 6, message = "图片最多 6 张")
    private List<String> images;
}
