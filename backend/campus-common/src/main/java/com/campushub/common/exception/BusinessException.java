package com.campushub.common.exception;

import com.campushub.common.enums.ResultCode;

/**
 * 业务异常
 * <p>
 * Service 层主动抛出,表示"可预期的业务失败"。由 {@link GlobalExceptionHandler} 统一捕获,
 * 响应中的 message 可直接展示给用户,不会打印堆栈。
 * </p>
 * <p>
 * 使用示例:
 * <pre>
 * throw new BusinessException(ResultCode.NOT_FOUND);                     // 使用枚举默认提示
 * throw new BusinessException(ResultCode.BAD_REQUEST, "标题不能为空");    // 自定义提示
 * throw new BusinessException(400, "自定义响应码");                       // ResultCode 未覆盖的场景
 * </pre>
 * </p>
 */
public class BusinessException extends RuntimeException {

    /** 业务响应码,与 {@link ResultCode} 的枚举值一致,前端据此判断错误类型 */
    private final int code;

    /**
     * 按枚举抛出异常,提示信息取枚举默认值
     *
     * @param resultCode 错误码枚举,如 {@link ResultCode#NOT_FOUND}
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    /**
     * 按枚举抛出异常,但自定义提示信息(覆盖枚举默认 message)
     *
     * @param resultCode 错误码枚举
     * @param message    自定义提示,将原样返回给前端
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    /**
     * 完全自定义响应码与提示,用于 {@link ResultCode} 未覆盖的业务场景
     *
     * @param code    自定义业务响应码
     * @param message 提示信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 获取业务响应码
     *
     * @return 业务响应码
     */
    public int getCode() { return code; }
}