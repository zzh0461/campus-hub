package com.campushub.campusnotificationservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置（notification 侧 = 消费者）
 *
 * 消费者负责声明"交换机 + 队列 + 绑定"三件套：
 * 声明是幂等的，服务每次启动都会确保它们存在。
 * 拓扑（一个事件类型一条专用队列，不要多类型共用一个队列）：
 *   campus.notification.topic --market.favorite-->   campus.notification.queue
 *   campus.notification.topic --activity.register--> campus.notification.activity.queue
 * 为什么分队列：一个队列上挂多个类型化 @RabbitListener 会被当成竞争消费者轮询分发，
 * 收藏消息可能投到活动监听器上反序列化失败——按路由键分队列才能各收各的
 *
 * @author CampusHub
 */
@Configuration
public class RabbitConfig {

    /** 通知域 topic 交换机（与 market 侧的常量值必须一致，靠约定对齐） */
    public static final String NOTIFICATION_EXCHANGE = "campus.notification.topic";

    /** 路由键：市场服务-商品被收藏事件 */
    public static final String ROUTING_MARKET_FAVORITE = "market.favorite";

    /** 路由键：活动服务-活动被报名事件 */
    public static final String ROUTING_ACTIVITY_REGISTER = "activity.register";

    /** 通知服务收件队列（市场收藏事件专用） */
    public static final String NOTIFICATION_QUEUE = "campus.notification.queue";

    /** 活动事件专用队列：与市场收藏队列分开，避免一个队列挂多个类型化监听器导致消息串台 */
    public static final String NOTIFICATION_ACTIVITY_QUEUE = "campus.notification.activity.queue";

    /** 消息序列化用 JSON（与 market 侧一致，两边必须同格式，只配一边必炸） */
    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /** 声明 topic 交换机（持久化） */
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE, true, false);
    }

    /** 声明持久化队列：MQ 重启后队列和里面的消息都还在 */
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }

    /** 绑定：市场队列只收路由键为 market.favorite 的消息 */
    @Bean
    public Binding marketFavoriteBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(notificationExchange())
                .with(ROUTING_MARKET_FAVORITE);
    }

    /** 声明活动事件专用持久化队列 */
    @Bean
    public Queue notificationActivityQueue() {
        return QueueBuilder.durable(NOTIFICATION_ACTIVITY_QUEUE).build();
    }

    /** 绑定：活动队列只收路由键为 activity.register 的消息 */
    @Bean
    public Binding activityRegisterBinding() {
        return BindingBuilder.bind(notificationActivityQueue())
                .to(notificationExchange())
                .with(ROUTING_ACTIVITY_REGISTER);
    }
}