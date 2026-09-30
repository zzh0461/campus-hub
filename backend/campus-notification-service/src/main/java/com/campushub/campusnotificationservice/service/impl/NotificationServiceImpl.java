package com.campushub.campusnotificationservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campushub.campusnotificationservice.domain.dto.NotificationQueryDTO;
import com.campushub.campusnotificationservice.domain.entity.Notification;
import com.campushub.campusnotificationservice.domain.vo.NotificationVO;
import com.campushub.campusnotificationservice.mapper.NotificationMapper;
import com.campushub.campusnotificationservice.service.NotificationService;
import com.campushub.common.enums.ResultCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.response.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 通知服务实现类
 *
 * @author CampusHub
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    public PageResult<NotificationVO> getNotifications(Long userId, NotificationQueryDTO query) {
        // 1.查询条件：userId 是数据隔离的根基（别人的通知从源头就查不到）；
        //   read 为 null 不筛选，true/false 才生效（包装类型三态的妙用）
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(query.getRead() != null, Notification::getIsRead, query.getRead())
                // 通知按时间倒序，最新的在最前
                .orderByDesc(Notification::getCreatedAt);

        // 2.分页查询
        Page<Notification> page = notificationMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        // 3.实体 → VO（字段简单，流内直转，不值得建 Converter 类）
        List<NotificationVO> records = page.getRecords().stream()
                .map(this::toVO)
                .toList();

        // 4.封装统一分页结构
        return PageResult.of(records, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    @Override
    public long getUnreadCount(Long userId) {
        // 命中联合索引 idx_user_read(user_id, is_read)，COUNT 走索引不扫表
        return notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, false));
    }

    @Override
    public void markRead(Long userId, Long id) {
        // 一条 UPDATE 搞定"查归属 + 改状态"：WHERE 同时带 id 和 userId，
        // 别人的通知根本匹配不上，不需要先 select 再判断（省一次查询，也没有并发窗口）
        int rows = notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .set(Notification::getIsRead, true)
                .eq(Notification::getId, id)
                .eq(Notification::getUserId, userId));
        if (rows == 0) {
            // 0 行 = 通知不存在或不属于当前用户，统一报 404（不区分，避免泄露"这条通知存在但不是你的"）
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
    }

    @Override
    public void markAllRead(Long userId) {
        // 只更新未读的行：已读的不碰，减少无效写入；
        // 0 行受影响是合法状态（本来就没有未读），不抛异常
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .set(Notification::getIsRead, true)
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, false));
    }

    @Override
    public void deleteNotification(Long userId, Long id) {
        // 同 markRead：归属校验融进 DELETE 的 WHERE，0 行报 404
        int rows = notificationMapper.delete(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getId, id)
                .eq(Notification::getUserId, userId));
        if (rows == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
    }

    /**
     * 实体 → VO：isRead → read 的名称转换发生在这里（数据库命名 → 前端契约命名）
     */
    private NotificationVO toVO(Notification n) {
        NotificationVO vo = new NotificationVO();
        vo.setId(n.getId());
        vo.setTitle(n.getTitle());
        vo.setContent(n.getContent());
        vo.setType(n.getType());
        vo.setRead(n.getIsRead());
        vo.setCreatedAt(n.getCreatedAt());
        return vo;
    }
}