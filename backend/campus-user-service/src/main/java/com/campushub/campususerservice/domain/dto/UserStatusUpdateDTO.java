package com.campushub.campususerservice.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户状态更新传输对象（后台管理用）
 *
 * 只允许切换 ACTIVE(正常) / DISABLED(禁用)，Service 内校验取值合法性。
 * 禁用后该用户将无法登录，用于封禁违规账号。
 *
 * @author CampusHub
 */
@Data
public class UserStatusUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 目标状态：ACTIVE / DISABLED */
    @NotBlank(message = "状态不能为空")
    private String status;
}
