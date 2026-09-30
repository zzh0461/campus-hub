package com.campushub.campusmarketservice.converter;

import com.campushub.campusmarketservice.client.UserClient;
import com.campushub.campusmarketservice.domain.entity.Category;
import com.campushub.campusmarketservice.domain.entity.Product;
import com.campushub.campusmarketservice.domain.vo.ProductVO;
import com.campushub.campusmarketservice.domain.vo.UserBriefVO;
import com.campushub.campusmarketservice.mapper.CategoryMapper;
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
 * 商品实体 → 商品VO 的公共转换器
 *
 * 抽取自 FavoriteServiceImpl / ProductServiceImpl 中重复的转换逻辑，两处共用。
 * 职责：复制同名字段、把 images(JSON字符串) 解析为 List、设置 favorite 收藏态、
 * 关联填充 categoryName（本地分类表），以及跨服务填充 sellerName/sellerAvatar（Feign 调 user-service）。
 *
 * @author CampusHub
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductConverter {

    private final ObjectMapper objectMapper;
    private final CategoryMapper categoryMapper;
    private final UserClient userClient;

    /**
     * 单个商品转换（详情页用）：单独查一次分类名
     *
     * @param product  商品实体
     * @param favorite 当前用户是否已收藏
     * @return 商品展示对象
     */
    public ProductVO toVO(Product product, boolean favorite) {
        ProductVO vo = convert(product, favorite);
        vo.setCategoryName(resolveCategoryName(product.getCategoryId()));
        // 单个卖家：详情页只有一件商品，查一次 user-service 填昵称/头像即可
        fillSeller(vo, resolveSeller(product.getSellerId()));
        return vo;
    }

    /**
     * 批量商品转换（列表页用）：一次性加载分类名映射，避免 N+1 查询
     *
     * @param products 商品实体列表
     * @param favorite 这一批统一的收藏态（商品列表传 false，收藏列表传 true）
     * @return 商品展示对象列表
     */
    public List<ProductVO> toVOList(List<Product> products, boolean favorite) {
        // 关键1：只查一次分类表，拿到 "分类ID→分类名" 映射，之后在内存里 O(1) 取名
        Map<Long, String> categoryNames = loadCategoryNameMap();
        // 关键2：只发一次 Feign 调用，批量拿到 "卖家ID→用户名片" 映射，避免 N+1 次远程调用
        Map<Long, UserBriefVO> sellers = loadSellerMap(products);
        return products.stream()
                .map(product -> {
                    ProductVO vo = convert(product, favorite);
                    vo.setCategoryName(categoryNames.get(product.getCategoryId()));
                    fillSeller(vo, sellers.get(product.getSellerId()));
                    return vo;
                })
                .toList();
    }


    /**
     * 实体→VO 的公共骨架：复制同名字段、解析图片、设置收藏态
     */
    private ProductVO convert(Product product, boolean favorite) {
        ProductVO vo = new ProductVO();
        // images 在实体是 String、在 VO 是 List<String>，类型不同，先排除再单独解析
        BeanUtils.copyProperties(product, vo, "images");
        vo.setImages(parseImages(product.getImages()));
        vo.setFavorite(favorite);
        return vo;
    }

    /**
     * 查单个分类名（详情用）；分类不存在返回 null
     */
    private String resolveCategoryName(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        Category category = categoryMapper.selectById(categoryId);
        return category == null ? null : category.getName();
    }

    /**
     * 一次性加载 "分类ID→分类名" 映射（列表用；分类表很小，全量加载比逐个查更省）
     */
    private Map<Long, String> loadCategoryNameMap() {
        return categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
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
            log.warn("商品图片 JSON 解析失败，返回空列表: {}", imagesJson, e);
            return Collections.emptyList();
        }
    }

    /**
     * 图片 List<String> → JSON 字符串（与 parseImages 互逆，发布/编辑写库时用）
     */
    public String toImagesJson(List<String> images) {
        if (images == null || images.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(images);
        } catch (Exception e) {
            log.warn("商品图片列表序列化失败，返回空数组: {}", images, e);
            return "[]";
        }
    }

    /**
     * 查单个卖家名片（详情用）；sellerId 为 null 或 user-service 降级时返回 null
     */
    private UserBriefVO resolveSeller(Long sellerId) {
        if (sellerId == null) {
            return null;
        }
        // 复用批量接口，传单元素列表；user-service 挂了会走 fallback 返回空列表
        List<UserBriefVO> sellers = userClient.listByIds(List.of(sellerId));
        return sellers.isEmpty() ? null : sellers.get(0);
    }

    /**
     * 批量加载 "卖家ID→用户名片" 映射（列表用）：
     * 先去重收集本页所有 sellerId，只发一次 Feign 调用，避免 N+1 次远程调用
     */
    private Map<Long, UserBriefVO> loadSellerMap(List<Product> products) {
        List<Long> sellerIds = products.stream()
                .map(Product::getSellerId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (sellerIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userClient.listByIds(sellerIds).stream()
                .collect(Collectors.toMap(UserBriefVO::getId, Function.identity()));
    }

    /**
     * 把卖家名片填进 VO（名片为 null 时保持空白，不报错）
     */
    private void fillSeller(ProductVO vo, UserBriefVO seller) {
        if (seller != null) {
            vo.setSellerName(seller.getNickname());
            vo.setSellerAvatar(seller.getAvatar());
        }
    }
}