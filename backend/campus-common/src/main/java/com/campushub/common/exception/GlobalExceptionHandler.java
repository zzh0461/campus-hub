package com.campushub.common.exception;

import com.campushub.common.response.R;
import com.campushub.common.enums.ResultCode;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 通过 {@link RestControllerAdvice} 拦截所有 Controller 抛出的异常,统一转换为
 * {@link R} 响应结构,保证异常响应与正常响应契约一致。
 * </p>
 * <p>
 * 处理顺序:Spring 按异常类型的匹配度选择最具体的 {@link ExceptionHandler} 方法,
 * 未被专门处理的异常最终落到 {@link #handleException(Exception)} 兜底。
 * </p>
 * <p>
 * HTTP 状态映射策略:
 * <ul>
 *   <li>401/403/500 同步 HTTP 状态码,方便网关鉴权、日志监控、告警联动;</li>
 *   <li>业务错误(400/404)保持 HTTP 200,由响应体 code 表达,避免业务失败刷满 4xx 日志。</li>
 * </ul>
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 业务异常:Service 主动抛出,消息可直接展示给用户 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<R<Void>> handleBusinessException(BusinessException e) {
        log.warn("业务异常 -> code={}, message={}", e.getCode(), e.getMessage());
        return build(e.getCode(), e.getMessage());
    }

    /** @Valid 请求体校验失败(字段级),拼接所有字段错误提示 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<R<Void>> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + " " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败 -> {}", message);
        return build(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /** 表单绑定校验失败(表单提交场景) */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<R<Void>> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return build(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /** @RequestParam / @PathVariable 上的约束校验失败(@Validated + @NotNull 等) */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<R<Void>> handleConstraintViolation(ConstraintViolationException e) {
        return build(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
    }

    /** 参数类异常统一处理:类型不匹配、缺少必传参数/请求头、请求体 JSON 解析失败、请求方式或媒体类型不支持 */
    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            MissingRequestHeaderException.class,
            HttpMessageNotReadableException.class,
            HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotSupportedException.class
    })
    public ResponseEntity<R<Void>> handleBadRequest(Exception e) {
        log.warn("请求参数异常 -> {}", e.getMessage());
        return build(ResultCode.BAD_REQUEST.getCode(), "请求参数格式错误");
    }

    /** 请求路径不存在(Spring 6.1 起由 NoResourceFoundException 表示) */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<R<Void>> handleNotFound(NoResourceFoundException e) {
        return build(ResultCode.NOT_FOUND.getCode(), "接口不存在");
    }

    /** 未认证:未携带 token 或 token 无效/过期(Spring Security 过滤器链抛出) */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<R<Void>> handleUnauthorized(AuthenticationException e) {
        return build(ResultCode.UNAUTHORIZED.getCode(), e.getMessage());
    }

    /** 无权限:已认证但无权访问目标资源(@PreAuthorize 等校验失败) */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<R<Void>> handleForbidden(AccessDeniedException e) {
        return build(ResultCode.FORBIDDEN.getCode(), ResultCode.FORBIDDEN.getMessage());
    }

    /** 系统兜底:捕获所有未处理异常,记完整堆栈,但只给用户通用提示 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<Void>> handleException(Exception e) {
        log.error("系统异常", e);
        return build(ResultCode.SERVER_ERROR.getCode(), ResultCode.SERVER_ERROR.getMessage());
    }

    /**
     * 构建统一响应体,并按响应码映射 HTTP 状态
     *
     * @param code    业务响应码
     * @param message 提示信息
     * @return 携带 HTTP 状态的统一响应
     */
    private ResponseEntity<R<Void>> build(int code, String message) {
        HttpStatus status = switch (code) {
            case 401 -> HttpStatus.UNAUTHORIZED;
            case 403 -> HttpStatus.FORBIDDEN;
            case 500 -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.OK;
        };
        return ResponseEntity.status(status).body(R.error(code, message));
    }
}