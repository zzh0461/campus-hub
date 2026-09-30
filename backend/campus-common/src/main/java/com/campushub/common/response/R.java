package com.campushub.common.response;

import com.campushub.common.enums.ResultCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 统一响应体
 * 项目所有 Controller 接口必须返回该结构，保证前后端契约一致。
 * 序列化后的 JSON 结构为：{@code {"code": 200, "message": "success", "data": {...}}}。
 *
 * @param <T> data 字段的数据类型，由调用方在静态方法上指定
 */
@Data
@Schema(description = "统一响应结果")
public class R<T> {

    /** 业务响应码，见 {@link ResultCode}；200 表示成功，其余为各类错误 */
    @Schema(description = "业务响应码:200 表示成功,其余为各类错误", example = "200")
    private final int code;

    /** 响应提示信息，成功时为 "success"，失败时为具体错误描述 */
    @Schema(description = "响应提示信息", example = "success")
    private final String message;

    /** 业务数据：成功时承载返回数据，失败时通常为 null */
    @Schema(description = "业务数据:成功时承载返回数据,失败时为 null")
    private final T data;

    /**
     * 私有构造器：禁止外部直接 new，只能通过 success() / error() 静态工厂方法创建，
     * 保证响应体的 code/message/data 三者组合始终符合约定。
     *
     * @param code    业务响应码
     * @param message 响应提示信息
     * @param data    业务数据
     */
    private R(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应：无业务数据
     *
     * @param <T> data 类型
     * @return code=200、message="success"、data=null 的响应体
     */
    public static <T> R<T> success() {
        return new R<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 成功响应：携带业务数据，使用默认提示 "success"
     *
     * @param data 业务数据
     * @param <T>  data 类型
     * @return code=200、message="success"、data=业务数据的响应体
     */
    public static <T> R<T> success(T data) {
        return new R<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应：携带业务数据与自定义提示信息
     *
     * @param data    业务数据
     * @param message 自定义成功提示信息（替代默认的 "success"）
     * @param <T>     data 类型
     * @return code=200、data=业务数据的响应体
     */
    public static <T> R<T> success(T data, String message) {
        return new R<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败响应：按枚举指定的响应码与默认提示信息返回
     *
     * @param resultCode 错误码枚举，如 {@link ResultCode#NOT_FOUND}
     * @param <T>        data 类型（失败时 data 为 null）
     * @return 对应错误码与默认提示的响应体
     */
    public static <T> R<T> error(ResultCode resultCode) {
        return new R<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 失败响应：按枚举指定响应码，但使用自定义提示信息
     *
     * @param resultCode 错误码枚举
     * @param message    自定义错误提示（覆盖枚举中的默认 message）
     * @param <T>        data 类型（失败时 data 为 null）
     * @return 对应错误码与自定义提示的响应体
     */
    public static <T> R<T> error(ResultCode resultCode, String message) {
        return new R<>(resultCode.getCode(), message, null);
    }

    /**
     * 失败响应：完全自定义响应码与提示信息
     * <p>
     * 用于 {@link ResultCode} 未覆盖的业务场景；
     * 建议新错误码优先补充到枚举中，保持全项目错误码统一。
     * </p>
     *
     * @param code    自定义业务响应码
     * @param message 错误提示信息
     * @param <T>     data 类型（失败时 data 为 null）
     * @return 自定义响应码与提示的响应体
     */
    public static <T> R<T> error(int code, String message) {
        return new R<>(code, message, null);
    }
}