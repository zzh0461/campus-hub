package com.campushub.campuscontentservice.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 失物招领状态更新传输对象
 *
 * 只允许发布者切换状态（OPEN 进行中 / RESOLVED 已解决），
 * 用于"失物找回了""招领被认领了"等场景把信息标记为已解决，或误标后重新打开。
 * 标题/描述等内容字段不在这里改，保持接口职责单一。
 *
 * @author CampusHub
 */
@Data
public class LostFoundStatusUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 目标状态：OPEN / RESOLVED，Service 内校验取值合法性 */
    @NotBlank(message = "状态不能为空")
    private String status;
}
