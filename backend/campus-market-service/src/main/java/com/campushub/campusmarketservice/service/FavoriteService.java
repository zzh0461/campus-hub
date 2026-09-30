package com.campushub.campusmarketservice.service;

import com.campushub.campusmarketservice.domain.vo.ProductVO;
import com.campushub.common.response.PageResult;

/**
 * 商品收藏服务接口
 *
 * 定义"我的收藏"相关业务方法，具体实现见 FavoriteServiceImpl。
 *
 * @author CampusHub
 */
public interface FavoriteService {

    /**
     * 分页查询指定用户收藏的商品
     *
     * @param userId   用户ID（后续由 user-service 通过 Feign 传入）
     * @param pageNum  页码，从 1 开始
     * @param pageSize 每页条数
     * @return 收藏商品的分页结果
     */
    PageResult<ProductVO> getMyFavorites(Long userId, long pageNum, long pageSize);

    /**
     * 取消收藏商品
     *
     * @param userId    用户ID
     * @param productId 商品ID
     */
    void unfavorite(Long userId, Long productId);

    /**
     * 收藏商品（幂等：已收藏则不重复插入）
     *
     * @param userId    用户ID
     * @param productId 商品ID
     */
    void favorite(Long userId, Long productId);

    /**
     * 判断用户是否已收藏某商品
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @return true=已收藏，false=未收藏
     */
    boolean isFavorited(Long userId, Long productId);

    /**
     * 删除某商品的全部收藏记录
     *
     * 商品被卖家删除时调用：campus_favorite 与 campus_product 之间没有外键约束，
     * 删商品不会级联清理收藏，需在此手动删除，避免留下指向已删商品的孤儿收藏。
     *
     * @param productId 商品ID
     * @return 删除的收藏记录数
     */
    int removeByProductId(Long productId);
}