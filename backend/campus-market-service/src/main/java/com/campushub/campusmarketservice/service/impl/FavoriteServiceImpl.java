package com.campushub.campusmarketservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campushub.campusmarketservice.config.RabbitConfig;
import com.campushub.campusmarketservice.converter.ProductConverter;
import com.campushub.campusmarketservice.domain.entity.Favorite;
import com.campushub.campusmarketservice.domain.entity.Product;
import com.campushub.campusmarketservice.domain.event.ProductFavoritedEvent;
import com.campushub.campusmarketservice.domain.vo.ProductVO;
import com.campushub.campusmarketservice.mapper.FavoriteMapper;
import com.campushub.campusmarketservice.mapper.ProductMapper;
import com.campushub.campusmarketservice.service.FavoriteService;
import com.campushub.common.enums.ResultCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.response.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 商品收藏服务实现类
 *
 * @author CampusHub
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final ProductMapper productMapper;
    private final ProductConverter productConverter;
    private final RabbitTemplate rabbitTemplate;

    /**
     * 分页查询指定用户收藏的商品
     * {@inheritDoc}
     *
     * @return 收藏商品的分页结果
     */
    @Override
    public PageResult<ProductVO> getMyFavorites(Long userId, long pageNum, long pageSize) {
        // 1.分页查询该用户的收藏记录（按收藏时间倒序，最新收藏的排前面）
        Page<Favorite> favoritePage = favoriteMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .orderByDesc(Favorite::getCreatedAt)
        );

        // 2.收藏为空则直接返回空分页结果，避免后续无谓查询
        List<Favorite> favorites = favoritePage.getRecords();
        if (favorites.isEmpty()) {
            return PageResult.of(Collections.emptyList(), 0L, pageNum, pageSize, 0L);
        }

        // 3.取出这一页收藏的商品ID列表（保持收藏顺序）
        List<Long> productIds = favorites.stream()
                .map(Favorite::getProductId)
                .toList();

        // 4.根据商品ID批量查询商品详情（等价 SQL: WHERE id IN (...)）
        List<Product> products = productMapper.selectByIds(productIds);

        // 5.转成 Map<商品ID, 商品>，便于按收藏顺序重排（selectBatchIds 不保证返回顺序）
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // 6.按收藏顺序取出商品实体（可能已被删除，过滤掉查不到的）
        List<Product> orderedProducts = productIds.stream()
                .map(productMap::get)
                .filter(Objects::nonNull)
                .toList();

        // 7.批量转 VO（内部一次性加载分类名，避免 N+1；收藏列表 favorite 恒为 true）
        List<ProductVO> records = productConverter.toVOList(orderedProducts, true);

        // 8.封装分页结果返回（total/pages 以收藏记录数为准）
        return PageResult.of(records, favoritePage.getTotal(), pageNum, pageSize, favoritePage.getPages());
    }

    /**
     * {@inheritDoc}
     *
     * @param userId    用户ID
     * @param productId 商品ID
     */
    @Override
    public void unfavorite(Long userId, Long productId) {
        // 1.按 user_id + product_id 精确删除收藏记录（等价 SQL: DELETE FROM campus_favorite WHERE user_id=? AND product_id=?）
        int deleted = favoriteMapper.delete(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getProductId, productId)
        );

        // 2.确实删掉了记录，才递减商品收藏数（避免重复取消把计数减成负数）
        if (deleted > 0) {
            productMapper.update(null,
                    new LambdaUpdateWrapper<Product>()
                            // 用 SQL 原子自减，而不是"查出来-1再写回"，避免并发丢失更新
                            .setSql("favorite_count = favorite_count - 1")
                            .eq(Product::getId, productId)
                            // 只有当前计数 > 0 才减，双保险防止出现负数
                            .gt(Product::getFavoriteCount, 0)
            );
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param userId    用户ID
     * @param productId 商品ID
     */
    public void favorite(Long userId, Long productId) {
        // 0.查商品：顺带补上存在性校验（此前收藏一个不存在的商品也会"静默成功"）
        //   发事件需要 sellerId/title，这次查询一石二鸟
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 1.幂等保护：campus_favorite 有 (user_id, product_id) 唯一约束，
        //   已收藏还硬插入会触发唯一键冲突报错，所以先查一下，已收藏就直接返回
        if (isFavorited(userId, productId)) {
            return;
        }

        // 2.插入收藏记录（id 自增、created_at 由数据库 DEFAULT CURRENT_TIMESTAMP 自动填充，都无需手动 set）
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        favoriteMapper.insert(favorite);

        // 3.商品收藏数 +1（SQL 原子自增，与 unfavorite 的自减完全对称）
        productMapper.update(null,
                new LambdaUpdateWrapper<Product>()
                        .setSql("favorite_count = favorite_count + 1")
                        .eq(Product::getId, productId)
        );

        // 4.发布"商品被收藏"事件，通知服务消费后给卖家发站内通知
        //   自己收藏自己的商品不发（没人需要这条通知）；
        //   第 3 步的 +1 发生在 SQL 里，内存中的 product 还是旧值，所以手动 +1 得到最新数
        if (!userId.equals(product.getSellerId())) {
            publishFavoritedEventQuietly(product, userId, product.getFavoriteCount() + 1L);
        }
    }

    /**
     * 发布收藏事件（Quietly 版）：与 ES 双写同一哲学——
     * MQ 挂了/网络抖动不能让用户的收藏操作失败，只记 error 日志。
     * 代价：通知可能丢一条（非关键数据，可接受）；关键业务才需要事务消息/本地消息表
     */
    private void publishFavoritedEventQuietly(Product product, Long actorId, Long favoriteCount) {
        try {
            ProductFavoritedEvent event = new ProductFavoritedEvent(
                    product.getSellerId(), product.getTitle(), actorId, favoriteCount);
            rabbitTemplate.convertAndSend(
                    RabbitConfig.NOTIFICATION_EXCHANGE,
                    RabbitConfig.ROUTING_MARKET_FAVORITE,
                    event);
        } catch (Exception e) {
            log.error("发布商品收藏事件失败，productId={}，通知将缺失但收藏已成功", product.getId(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @return
     */
    @Override
    public boolean isFavorited(Long userId, Long productId) {
        // 统计 campus_favorite 中 (user_id, product_id) 的记录数，>0 即已收藏
        Long count = favoriteMapper.selectCount(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getProductId, productId)
        );
        return count != null && count > 0;
    }

    /**
     * {@inheritDoc}
     *
     * @param productId 商品ID
     * @return
     */
    @Override
    public int removeByProductId(Long productId) {
        // 删除该商品的全部收藏记录（等价 SQL: DELETE FROM campus_favorite WHERE product_id=?）
        // 无需回写 favorite_count——商品本身马上要被删除，计数已无意义
        return favoriteMapper.delete(
                new LambdaQueryWrapper<Favorite>().eq(Favorite::getProductId, productId));
    }
}