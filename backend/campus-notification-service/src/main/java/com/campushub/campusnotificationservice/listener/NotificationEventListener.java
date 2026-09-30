package com.campushub.campusnotificationservice.listener;

import com.campushub.campusnotificationservice.config.RabbitConfig;
import com.campushub.campusnotificationservice.domain.entity.Notification;
import com.campushub.campusnotificationservice.domain.event.ActivityRegisteredEvent;
import com.campushub.campusnotificationservice.domain.event.ProductFavoritedEvent;
import com.campushub.campusnotificationservice.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 通知事件监听器：从队列消费业务事件，拼装文案并落库
 *
 * 职责分界的体现：market 只发"发生了什么"，"通知长什么样"（标题/文案/类型）全在这里决定——
 * 将来改文案、加推送渠道，market 一行代码都不用动
 *
 * @author CampusHub
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationMapper notificationMapper;

    /**
     * 消费"商品被收藏"事件：给卖家写一条 MARKET 类型的未读通知
     */
    @RabbitListener(queues = RabbitConfig.NOTIFICATION_QUEUE)
    public void onProductFavorited(ProductFavoritedEvent event) {
        try {
            Notification notification = new Notification();
            notification.setUserId(event.getSellerId());
            notification.setTitle("你发布的商品被收藏");
            notification.setContent(String.format("商品「%s」新增收藏，当前共 %d 人收藏。",
                    event.getProductTitle(), event.getFavoriteCount()));
            notification.setType("MARKET");
            notification.setIsRead(false);
            // createdAt 保持 null：MyBatis-Plus 默认不把 null 字段拼进 INSERT，
            // 数据库 DEFAULT CURRENT_TIMESTAMP 自动填充（跟 favorite 表同一套路）
            notificationMapper.insert(notification);
            log.info("消费收藏事件成功，通知已落库：sellerId={}, product={}",
                    event.getSellerId(), event.getProductTitle());
        } catch (Exception e) {
            // 吃掉异常是有讲究的：如果往外抛，RabbitMQ 会把这条消息重新入队 →
            // 再次消费 → 再次失败 → 无限循环刷爆日志和 CPU。
            // 代价是这条通知丢了（可接受）；生产级做法是转投死信队列(DLQ)人工补偿，以后再说
            log.error("消费收藏事件失败，消息丢弃：{}", event, e);
        }
    }

    /**
     * 消费"活动被报名"事件：给活动组织者写一条 ACTIVITY 类型的未读通知。
     * 注意监听的是活动专用队列（NOTIFICATION_ACTIVITY_QUEUE），与收藏队列分开
     */
    @RabbitListener(queues = RabbitConfig.NOTIFICATION_ACTIVITY_QUEUE)
    public void onActivityRegistered(ActivityRegisteredEvent event) {
        try {
            Notification notification = new Notification();
            notification.setUserId(event.getOrganizerId());
            notification.setTitle("你发布的活动有新报名");
            notification.setContent(String.format("活动「%s」新增报名，当前共 %d 人参加。",
                    event.getActivityTitle(), event.getCurrentParticipants()));
            notification.setType("ACTIVITY");
            notification.setIsRead(false);
            // createdAt 保持 null：MyBatis-Plus 不拼 null 字段，数据库 DEFAULT CURRENT_TIMESTAMP 自动填充
            notificationMapper.insert(notification);
            log.info("消费活动报名事件成功，通知已落库：organizerId={}, activity={}",
                    event.getOrganizerId(), event.getActivityTitle());
        } catch (Exception e) {
            // 同收藏事件：吃掉异常防 RabbitMQ 无限 requeue 死循环，代价丢消息，生产级用 DLQ
            log.error("消费活动报名事件失败，消息丢弃：{}", event, e);
        }
    }
}