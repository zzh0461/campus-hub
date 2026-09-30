package com.campushub.campusadminservice.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * admin 服务 Jackson 定制：前端 ISO 时间串的时区归一化
 *
 * 背景：前端提交活动起止时间用的是 new Date(...).toISOString()，产物带 Z 后缀（UTC），
 * 如北京时间 18:00 会变成 "2026-10-01T10:00:00.000Z"。Jackson 2.15 的 LocalDateTime
 * 反序列化器虽然"能"解析 Z 串，但保留的是 UTC 墙上时间（10:00），直接入库就差了 8 小时。
 *
 * 只有 admin 后台的"新建/编辑活动"需要前端传时间，故把定制收在 admin 服务内（最小影响面），
 * 其余服务与 Feign 内部链路（裸 ISO 串，无时区）不受影响。
 *
 * 规则：无时区的裸 ISO 串按原值解析；带 Z/偏移量的串按 Asia/Shanghai 换算成墙上时间。
 * deserializerByType 在 builder 内部最后注册，保证覆盖 JavaTimeModule 的默认反序列化器。
 *
 * @author CampusHub
 */
@Configuration
public class JacksonConfig {

    /** 项目约定的业务时区：与数据库 serverTimezone、部署环境保持一致 */
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer asiaTimeJsonCustomizer() {
        return builder -> builder.deserializerByType(
                LocalDateTime.class, new AsiaLenientLocalDateTimeDeserializer());
    }

    /**
     * 宽容的 LocalDateTime 反序列化器
     *
     * 带时区标记（Z / +08:00）的 ISO 串先解析成 OffsetDateTime，
     * 再换算到业务时区取墙上时间；无时区标记的串直接按 LocalDateTime 解析。
     */
    static class AsiaLenientLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

        @Override
        public LocalDateTime deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
            String text = parser.getValueAsString();
            if (text == null || text.isBlank()) {
                return null;
            }
            text = text.trim();
            try {
                // 裸 ISO 串（Feign 内部链路的常规格式）直接解析
                return LocalDateTime.parse(text);
            } catch (Exception ignored) {
                // 带时区标记：换算到业务时区，保留管理员的"本地直觉"时间
                return OffsetDateTime.parse(text).atZoneSameInstant(BUSINESS_ZONE).toLocalDateTime();
            }
        }
    }
}
