package com.campushub.campusnotificationservice.service;

import com.campushub.campusnotificationservice.domain.dto.NotificationQueryDTO;
import com.campushub.campusnotificationservice.domain.vo.NotificationVO;
import com.campushub.common.response.PageResult;

/**
 * 通知服务接口
 *
 * @author CampusHub
 */
public interface NotificationService {

    /**
     * 分页查询当前用户的通知（可按已读状态筛选），按时间倒序
     *
     * @param userId 当前登录用户ID（由后端从 JWT 上下文注入，不接受前端传）
     * @param query  分页 + 已读筛选参数
     * @return 通知分页结果
     */
    PageResult<NotificationVO> getNotifications(Long userId, NotificationQueryDTO query);

    /**
     * 查询当前用户的未读通知数量（顶栏小红点）
     *
     * @param userId 当前登录用户ID
     * @return 未读数量
     */
    long getUnreadCount(Long userId);

    /**
     * 标记单条通知已读：UPDATE 的 WHERE 同时带 id + userId（归属校验融进 SQL），
     * 影响行数为 0 说明通知不存在或不属于当前用户，抛 404。
     * 幂等：已读的通知重复标记不报错
     *
     * @param userId 当前登录用户ID
     * @param id     通知ID
     */
    void markRead(Long userId, Long id);

    /**
     * 全部标记已读：只更新"未读"的行。0 行受影响是合法状态（本来就没有未读），不抛异常
     *
     * @param userId 当前登录用户ID
     */
    void markAllRead(Long userId);

    /**
     * 删除通知：DELETE 的 WHERE 同时带 id + userId，0 行受影响抛 404
     *
     * @param userId 当前登录用户ID
     * @param id     通知ID
     */
    void deleteNotification(Long userId, Long id);
}