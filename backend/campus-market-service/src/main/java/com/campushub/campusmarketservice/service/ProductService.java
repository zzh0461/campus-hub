package com.campushub.campusmarketservice.service;

import com.campushub.campusmarketservice.domain.dto.AdminProductQueryDTO;
import com.campushub.campusmarketservice.domain.dto.MyProductQueryDTO;
import com.campushub.campusmarketservice.domain.dto.ProductPublishDTO;
import com.campushub.campusmarketservice.domain.dto.ProductQueryDTO;
import com.campushub.campusmarketservice.domain.dto.ProductStatusUpdateDTO;
import com.campushub.campusmarketservice.domain.dto.ProductUpdateDTO;
import com.campushub.campusmarketservice.domain.vo.CategoryVO;
import com.campushub.campusmarketservice.domain.vo.MarketStatsVO;
import com.campushub.campusmarketservice.domain.vo.ProductVO;
import com.campushub.common.response.PageResult;

import java.util.List;

/**
 * 商品服务接口
 *
 * @author CampusHub
 */
public interface ProductService {

    /**
     * 分页查询在售商品（支持关键词/分类/价格筛选与排序）
     *
     * @param query 查询参数
     * @return 商品分页结果
     */
    PageResult<ProductVO> getProducts(ProductQueryDTO query);

    /**
     * 查询商品详情：浏览量 +1，并按当前用户计算收藏态
     *
     * @param productId 商品ID
     * @param userId    当前登录用户ID（用于计算 favorite，可为 null）
     * @return 商品详情 VO
     */
    ProductVO getProductDetail(Long productId, Long userId);

    /**
     * 查询全部商品分类（按排序值升序，供前端筛选下拉框使用）
     *
     * @return 分类列表
     */
    List<CategoryVO> getCategories();

    /**
     * 发布商品：卖家ID取自登录上下文，状态默认在售，计数/时间由数据库默认值填充
     *
     * @param sellerId 当前登录用户ID（卖家，由后端注入，不接受前端传）
     * @param dto      发布参数
     * @return 发布后的商品 VO
     */
    ProductVO publishProduct(Long sellerId, ProductPublishDTO dto);

    /**
     * 查询"我的商品"：仅返回当前登录卖家自己发布的商品（含全部状态），可按状态筛选
     *
     * @param sellerId 当前登录用户ID（由后端注入，不接受前端传）
     * @param query    分页 + 状态筛选参数
     * @return 商品分页结果
     */
    PageResult<ProductVO> getMyProducts(Long sellerId, MyProductQueryDTO query);

    /**
     * 更新商品（编辑内容 / 上下架）：仅卖家本人可改，否则抛 403
     *
     * @param productId 商品ID
     * @param userId    当前登录用户ID（用于归属校验，不接受前端传）
     * @param dto       更新参数（status 可选）
     * @return 更新后的商品 VO
     */
    ProductVO updateProduct(Long productId, Long userId, ProductUpdateDTO dto);

    /**
     * 删除商品：仅卖家本人可删，否则抛 403；连带清理该商品的收藏记录
     *
     * @param productId 商品ID
     * @param userId    当前登录用户ID（用于归属校验，不接受前端传）
     */
    void deleteProduct(Long productId, Long userId);

    /**
     * ES 全文搜索：关键词多字段匹配（标题权重 x2）+ 在售/分类过滤，
     * 命中 id 回 MySQL 批量补展示字段，并保持 ES 的相关度/排序顺序
     *
     * @param query 搜索参数（keyword 必填语义，前端搜索框入口）
     * @return 商品分页结果
     */
    PageResult<ProductVO> searchProducts(ProductQueryDTO query);

    /**
     * 首页热门推荐：在售商品按收藏数、浏览量倒序取前 N 条
     * <p>
     * 计数真相在 MySQL（浏览/收藏不双写 ES，ES 里的计数是过期快照），故不走 ES
     *
     * @return 推荐商品列表
     */
    List<ProductVO> getRecommendedProducts();

    /**
     * 后台分页查询商品（供 admin-service 调用）
     *
     * 与用户侧列表的区别：不做"只在售"过滤，全部状态都返回；
     * 关键词对标题模糊匹配，按发布时间倒序。
     *
     * @param query 分页 + 关键词参数
     * @return 商品分页结果（含卖家名片，favorite 恒为 false）
     */
    PageResult<ProductVO> pageProductsForAdmin(AdminProductQueryDTO query);

    /**
     * 后台更新商品状态（上架/下架/标记售出），无需卖家归属校验
     *
     * 状态白名单 ON_SALE / OFF_SHELF / SOLD；商品不存在抛 404。
     *
     * @param productId 商品ID
     * @param dto       状态更新参数
     * @return 更新后的商品 VO
     */
    ProductVO updateProductStatusByAdmin(Long productId, ProductStatusUpdateDTO dto);

    /**
     * 后台删除商品：不做归属校验，连带清理收藏记录与 ES 文档
     *
     * @param productId 商品ID
     */
    void deleteProductByAdmin(Long productId);

    /**
     * 后台市场统计（供 admin-service 的 Dashboard 聚合）
     *
     * 商品总数、今日新增、近 14 天发布趋势（日期轴连续，无发布的日期补 0）。
     *
     * @return 市场统计
     */
    MarketStatsVO getMarketStats();
}