package com.campushub.campusmarketservice.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置（market 侧 = 生产者）
 *
 * 交换机命名：campus.{目标域}.{类型}；路由键命名：{来源服务}.{事件}
 * 生产者也声明交换机：声明是幂等的（已存在则跳过），
 * 避免"notification 还没启动过、交换机不存在、消息发出去被静默丢弃"
 *
 * @author CampusHub
 */
@Configuration
public class RabbitConfig {

    /** 通知域 topic 交换机：activity 将来的事件也走它 */
    public static final String NOTIFICATION_EXCHANGE = "campus.notification.topic";

    /** 路由键：市场服务-商品被收藏事件 */
    public static final String ROUTING_MARKET_FAVORITE = "market.favorite";

    /**
     * 消息序列化改为 JSON：默认的 Java 原生序列化要求两边类完全一致且跨语言不通，
     * JSON 只对齐字段名，松耦合。生产者/消费者两边都必须配，只配一边必炸
     */
    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 挂 JSON 转换器的 RabbitTemplate（Spring Boot 自动装配时会拾取容器里的 MessageConverter，
     * 这里显式绑定，语义更清晰）
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter jackson2JsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter);
        return template;
    }

    /** 声明 topic 交换机（持久化：MQ 重启后交换机还在） */
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE, true, false);
    }
}