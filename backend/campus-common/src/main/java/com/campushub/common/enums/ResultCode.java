package com.campushub.common.enums;

import com.campushub.common.response.R;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 统一响应码枚举
 * 业务响应码（code）及对应的提示信息（message），供 {@link R} 统一响应体使用。
 */
@Getter
@Schema(description = "统一响应码枚举")
public enum ResultCode {

    /** 成功：请求处理正常，前端正常渲染返回的数据 */
    SUCCESS(200, "success"),

    /** 参数或业务错误：请求参数不合法或业务校验未通过，前端提示 message */
    BAD_REQUEST(400, "请求参数错误"),

    /** 未登录或登录已过期：前端应清除 token 并跳转登录页 */
    UNAUTHORIZED(401, "未登录或登录已过期"),

    /** 无权限：当前用户无权执行该操作，前端提示「没有权限」，后台路由跳转 403 页 */
    FORBIDDEN(403, "没有权限执行该操作"),

    /** 资源不存在：请求的目标资源不存在或已被删除，前端提示 message */
    NOT_FOUND(404, "资源不存在"),

    /** 服务器异常：未捕获的运行时异常，前端统一提示「服务器异常」 */
    SERVER_ERROR(500, "服务器异常,请稍后重试");

    /** 业务响应码，与 HTTP 状态码语义对齐（200/400/401/403/404/500） */
    private final int code;

    /** 响应提示信息，用于前端直接展示给用户 */
    private final String message;

    /**
     * 枚举构造器
     *
     * @param code    业务响应码
     * @param message 响应提示信息
     */
    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}