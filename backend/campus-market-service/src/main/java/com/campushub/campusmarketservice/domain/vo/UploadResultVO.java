package com.campushub.campusmarketservice.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 图片上传结果 VO
 *
 * 字段与前端 types/common.ts 的 UploadResult 契约对齐，只返回一个可访问的图片 URL。
 *
 * @author CampusHub
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 上传后可访问的图片 URL */
    private String url;
}