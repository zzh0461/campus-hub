package com.campushub.campusnotificationservice.controller;

import com.campushub.campusnotificationservice.domain.dto.NotificationQueryDTO;
import com.campushub.campusnotificationservice.domain.vo.NotificationVO;
import com.campushub.campusnotificationservice.domain.vo.UnreadCountVO;
import com.campushub.campusnotificationservice.service.NotificationService;
import com.campushub.common.context.UserContext;
import com.campushub.common.enums.ResultCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.response.PageResult;
import com.campushub.common.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通知控制器
 *
 * 路径在 /api/notifications/** 下，网关已配好路由且强制 JWT 鉴权（白名单外）。
 * 用户身份统一从 UserContext 获取（网关校验 JWT 后透传的 X-User-Id 填充），
 * 不接受前端传 userId——通知是隐私数据，身份必须来自可信链路。
 *
 * @author CampusHub
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 分页查询我的通知（可按已读状态筛选：read=true/false，不传查全部）
     *
     * @param query 分页 + 筛选参数
     * @return 通知分页结果
     */
    @GetMapping
    public R<PageResult<NotificationVO>> list(NotificationQueryDTO query) {
        return R.success(notificationService.getNotifications(getUserId(), query));
    }

    /**
     * 未读通知数量（顶栏铃铛小红点）
     *
     * @return { count: n }
     */
    @GetMapping("/unread-count")
    public R<UnreadCountVO> unreadCount() {
        return R.success(new UnreadCountVO(notificationService.getUnreadCount(getUserId())));
    }

    /**
     * 标记单条已读：通知不存在或不属于当前用户时返回 404
     *
     * @param id 通知ID
     * @return 空响应（前端只关心 code=200）
     */
    @PutMapping("/{id}/read")
    public R<Void> markRead(@PathVariable("id") Long id) {
        notificationService.markRead(getUserId(), id);
        return R.success();
    }

    /**
     * 全部标记已读
     * <p>
     * 路由说明：/read-all 是单段字面量路径，与两段的 /{id}/read 结构不同，不会冲突
     *
     * @return 空响应
     */
    @PutMapping("/read-all")
    public R<Void> markAllRead() {
        notificationService.markAllRead(getUserId());
        return R.success();
    }

    /**
     * 删除通知：通知不存在或不属于当前用户时返回 404
     *
     * @param id 通知ID
     * @return 空响应
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable("id") Long id) {
        notificationService.deleteNotification(getUserId(), id);
        return R.success();
    }

    /**
     * 获取当前登录用户ID，未登录则抛 401
     */
    @NonNull
    private static Long getUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }
}