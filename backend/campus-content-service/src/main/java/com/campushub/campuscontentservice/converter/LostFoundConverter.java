package com.campushub.campuscontentservice.converter;

import com.campushub.campuscontentservice.client.UserClient;
import com.campushub.campuscontentservice.domain.entity.LostFound;
import com.campushub.campuscontentservice.domain.vo.LostFoundVO;
import com.campushub.campuscontentservice.domain.vo.UserBriefVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 失物招领实体 → VO 的公共转换器
 *
 * 职责：复制同名字段、把 images(JSON字符串) 解析为 List、
 * 跨服务填充 publisherName/publisherAvatar（Feign 调 user-service）。
 * 复刻 market 的 ProductConverter 范式：列表批量填充避免 N+1 次远程调用。
 *
 * @author CampusHub
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LostFoundConverter {

    private final ObjectMapper objectMapper;
    private final UserClient userClient;

    /**
     * 单条转换（详情页用）：单独查一次发布者名片
     *
     * @param item 失物招领实体
     * @return 展示对象
     */
    public LostFoundVO toVO(LostFound item) {
        LostFoundVO vo = convert(item);
        // 单条：详情页只有一条记录，查一次 user-service 填昵称/头像即可
        fillPublisher(vo, resolvePublisher(item.getPublisherId()));
        return vo;
    }

    /**
     * 批量转换（列表页用）：一次性加载发布者名片映射，避免 N+1 次远程调用
     *
     * @param items 失物招领实体列表
     * @return 展示对象列表
     */
    public List<LostFoundVO> toVOList(List<LostFound> items) {
        // 只发一次 Feign 调用，批量拿到 "发布者ID→用户名片" 映射
        Map<Long, UserBriefVO> publishers = loadPublisherMap(items);
        return items.stream()
                .map(item -> {
                    LostFoundVO vo = convert(item);
                    fillPublisher(vo, publishers.get(item.getPublisherId()));
                    return vo;
                })
                .toList();
    }

    /**
     * 实体→VO 的公共骨架：复制同名字段、解析图片
     */
    private LostFoundVO convert(LostFound item) {
        LostFoundVO vo = new LostFoundVO();
        // images 在实体是 String、在 VO 是 List<String>，类型不同，先排除再单独解析
        BeanUtils.copyProperties(item, vo, "images");
        vo.setImages(parseImages(item.getImages()));
        return vo;
    }

    /**
     * 图片 JSON 字符串（如 ["url1","url2"]）→ List<String>
     */
    public List<String> parseImages(String imagesJson) {
        if (imagesJson == null || imagesJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("失物招领图片 JSON 解析失败，返回空列表: {}", imagesJson, e);
            return Collections.emptyList();
        }
    }

    /**
     * 图片 List<String> → JSON 字符串（与 parseImages 互逆，发布写库时用）
     */
    public String toImagesJson(List<String> images) {
        if (images == null || images.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(images);
        } catch (Exception e) {
            log.warn("失物招领图片列表序列化失败，返回空数组: {}", images, e);
            return "[]";
        }
    }

    /**
     * 查单个发布者名片（详情用）；publisherId 为 null 或 user-service 降级时返回 null
     */
    private UserBriefVO resolvePublisher(Long publisherId) {
        if (publisherId == null) {
            return null;
        }
        // 复用批量接口，传单元素列表；user-service 挂了会走 fallback 返回空列表
        List<UserBriefVO> publishers = userClient.listByIds(List.of(publisherId));
        return publishers.isEmpty() ? null : publishers.get(0);
    }

    /**
     * 批量加载 "发布者ID→用户名片" 映射（列表用）：
     * 先去重收集本页所有 publisherId，只发一次 Feign 调用，避免 N+1 次远程调用
     */
    private Map<Long, UserBriefVO> loadPublisherMap(List<LostFound> items) {
        List<Long> publisherIds = items.stream()
                .map(LostFound::getPublisherId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (publisherIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userClient.listByIds(publisherIds).stream()
                .collect(Collectors.toMap(UserBriefVO::getId, Function.identity()));
    }

    /**
     * 把发布者名片填进 VO（名片为 null 时保持空白，不报错）
     */
    private void fillPublisher(LostFoundVO vo, UserBriefVO publisher) {
        if (publisher != null) {
            vo.setPublisherName(publisher.getNickname());
            vo.setPublisherAvatar(publisher.getAvatar());
        }
    }
}
