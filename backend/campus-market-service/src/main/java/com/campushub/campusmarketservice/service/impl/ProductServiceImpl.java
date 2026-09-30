package com.campushub.campusmarketservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campushub.campusmarketservice.converter.ProductConverter;
import com.campushub.campusmarketservice.domain.document.ProductDocument;
import com.campushub.campusmarketservice.domain.dto.AdminProductQueryDTO;
import com.campushub.campusmarketservice.domain.dto.DailyCountDTO;
import com.campushub.campusmarketservice.domain.dto.MyProductQueryDTO;
import com.campushub.campusmarketservice.domain.dto.ProductPublishDTO;
import com.campushub.campusmarketservice.domain.dto.ProductQueryDTO;
import com.campushub.campusmarketservice.domain.dto.ProductStatusUpdateDTO;
import com.campushub.campusmarketservice.domain.dto.ProductUpdateDTO;
import com.campushub.campusmarketservice.domain.entity.Category;
import com.campushub.campusmarketservice.domain.entity.Product;
import com.campushub.campusmarketservice.domain.vo.CategoryVO;
import com.campushub.campusmarketservice.domain.vo.MarketStatsVO;
import com.campushub.campusmarketservice.domain.vo.ProductVO;
import com.campushub.campusmarketservice.domain.vo.TrendPointVO;
import com.campushub.campusmarketservice.mapper.CategoryMapper;
import com.campushub.campusmarketservice.mapper.ProductMapper;
import com.campushub.campusmarketservice.service.FavoriteService;
import com.campushub.campusmarketservice.service.ProductService;
import com.campushub.campusmarketservice.service.ProductSyncService;
import com.campushub.common.enums.ResultCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.response.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 商品服务实现类
 *
 * @author CampusHub
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    /**
     * 在售状态：市场列表只展示在售商品
     */
    private static final String STATUS_ON_SALE = "ON_SALE";

    /**
     * 下架状态：后台状态白名单之一
     */
    private static final String STATUS_OFF_SHELF = "OFF_SHELF";

    /**
     * 已售出状态：后台状态白名单之一
     */
    private static final String STATUS_SOLD = "SOLD";

    /**
     * 首页推荐条数：一排卡片 8 个够用
     */
    private static final int RECOMMEND_SIZE = 8;

    /**
     * Dashboard 趋势图天数：近 14 天（含今天），与前端图表数据范围一致
     */
    private static final int TREND_DAYS = 14;


    private final ProductMapper productMapper;
    private final ProductConverter productConverter;
    private final FavoriteService favoriteService;
    private final CategoryMapper categoryMapper;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ProductSyncService productSyncService;

    @Override
    public PageResult<ProductVO> getProducts(ProductQueryDTO query) {
        // 1.构建查询条件：只看在售 + 各筛选项"有值才生效"
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, STATUS_ON_SALE)
                // 第一个参数是"是否应用此条件"：keyword 有值才 LIKE 标题
                .like(StringUtils.hasText(query.getKeyword()), Product::getTitle, query.getKeyword())
                .eq(query.getCategoryId() != null, Product::getCategoryId, query.getCategoryId())
                .ge(query.getMinPrice() != null, Product::getPrice, query.getMinPrice())
                .le(query.getMaxPrice() != null, Product::getPrice, query.getMaxPrice());

        // 2.排序
        applySort(wrapper, query.getSort());

        // 3.分页查询
        return getProductVOPageResult(query.getPageNum(), query.getPageSize(), wrapper);
    }

    /**
     * {@inheritDoc}
     *
     * @param productId 商品ID
     * @param userId    当前登录用户ID（用于计算 favorite，可为 null）
     * @return
     */
    @Override
    public ProductVO getProductDetail(Long productId, Long userId) {
        // 1.查商品，不存在则抛 404（前端会显示"商品加载失败"）
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "商品不存在或已删除");
        }

        // 2.浏览量 +1：用 SQL 原子自增写库，避免并发丢失更新
        productMapper.update(null,
                new LambdaUpdateWrapper<Product>()
                        .setSql("view_count = view_count + 1")
                        .eq(Product::getId, productId)
        );
        // 同步更新内存对象，让本次返回的 VO 就带上"你这次浏览"后的最新浏览量
        product.setViewCount(product.getViewCount() + 1);

        // 3.按当前用户计算收藏态（未登录 userId 为 null 时，短路跳过查询，视为未收藏）
        boolean favorite = userId != null && favoriteService.isFavorited(userId, productId);

        // 4.转 VO 返回（favorite 用真实值，详情页据此显示"收藏/已收藏"）
        return productConverter.toVO(product, favorite);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    public List<CategoryVO> getCategories() {
        // 查询全部分类，按 sort_order 升序（越小越靠前），再转成精简的 CategoryVO
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSortOrder)
        );
        return categories.stream()
                .map(category -> new CategoryVO(category.getId(), category.getName()))
                .toList();
    }

    /**
     * {@inheritDoc}
     *
     * @param sellerId 当前登录用户ID（卖家，由后端注入，不接受前端传）
     * @param dto      发布参数
     * @return
     */
    @Override
    public ProductVO publishProduct(Long sellerId, ProductPublishDTO dto) {
        // 1.DTO → 实体：同名字段拷贝；images 类型不同(List→JSON字符串)排除后单独序列化
        Product product = new Product();
        BeanUtils.copyProperties(dto, product, "images");
        product.setImages(productConverter.toImagesJson(dto.getImages()));

        // 2.受控字段由后端填充，绝不信任前端：卖家=登录用户，状态=在售
        product.setSellerId(sellerId);
        product.setStatus(STATUS_ON_SALE);

        // 3.入库：id 自增、created_at/updated_at、favorite_count/view_count 均由数据库默认值填充
        productMapper.insert(product);

        // 4.回查拿到数据库填充后的完整记录（id/时间/计数），再转 VO 返回给前端跳转详情用
        Product saved = productMapper.selectById(product.getId());

        // 5.双写 ES：新商品立刻可被搜索（失败不影响发布主链路，靠 reimport 兜底修复）
        syncToEsQuietly(saved);

        return productConverter.toVO(saved, false);
    }

    /**
     * {@inheritDoc}
     *
     * @param sellerId 当前登录用户ID（卖家）
     * @param query    分页 + 状态筛选参数
     * @return
     */
    @Override
    public PageResult<ProductVO> getMyProducts(Long sellerId, MyProductQueryDTO query) {
        // 1.只查自己发布的商品；状态可选（"全部"页签不传 status，条件自动不生效）
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getSellerId, sellerId)
                .eq(StringUtils.hasText(query.getStatus()), Product::getStatus, query.getStatus())
                // 自己的商品按发布时间倒序，最新发布的排最前
                .orderByDesc(Product::getCreatedAt);

        // 2.分页查询
        return getProductVOPageResult(query.getPageNum(), query.getPageSize(), wrapper);
    }

    /**
     * {@inheritDoc}
     *
     * @param productId 商品ID
     * @param userId    当前登录用户ID
     * @param dto       更新参数
     * @return
     */
    @Override
    public ProductVO updateProduct(Long productId, Long userId, ProductUpdateDTO dto) {
        // 1.查商品：不存在 → 404
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "商品不存在或已删除");
        }

        // 2.归属校验：只有卖家本人能改，否则 → 403（防止越权改他人商品）
        if (!product.getSellerId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能修改自己发布的商品");
        }

        // 3.只更新可编辑列，计数/时间不碰（避免覆盖并发中的收藏数、浏览量）
        productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .set(Product::getTitle, dto.getTitle())
                .set(Product::getCategoryId, dto.getCategoryId())
                .set(Product::getPrice, dto.getPrice())
                .set(Product::getDescription, dto.getDescription())
                .set(Product::getImages, productConverter.toImagesJson(dto.getImages()))
                // status 有值才改：编辑内容时不传，保持原状态；上下架时才切换
                .set(StringUtils.hasText(dto.getStatus()), Product::getStatus, dto.getStatus())
                .eq(Product::getId, productId));

        // 4.回查最新记录再转 VO 返回（前端保存后跳详情页展示）
        Product saved = productMapper.selectById(productId);

        // 5.双写 ES：编辑内容/上下架后同步，保证搜索结果与 MySQL 一致（下架商品应从搜索中消失）
        syncToEsQuietly(saved);

        return productConverter.toVO(saved, false);
    }

    /**
     * {@inheritDoc}
     *
     * @param productId 商品ID
     * @param userId    当前登录用户ID
     */
    @Override
    @Transactional
    public void deleteProduct(Long productId, Long userId) {
        // 1.查商品：不存在 → 404
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "商品不存在或已删除");
        }

        // 2.归属校验：只有卖家本人能删，否则 → 403（防止越权删他人商品）
        if (!product.getSellerId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能删除自己发布的商品");
        }

        // 3.先删该商品的所有收藏（无外键约束，需手动清理，避免孤儿收藏）
        favoriteService.removeByProductId(productId);

        // 4.再删商品本身（@Transactional 保证第 3、4 步要么都成功要么都回滚）
        productMapper.deleteById(productId);

        // 5.双写 ES：删除搜索文档，避免搜到已删除的"幽灵商品"
        removeFromEsQuietly(productId);
    }

    /**
     * {@inheritDoc}
     *
     * @param query 搜索参数
     * @return
     */
    @Override
    public PageResult<ProductVO> searchProducts(ProductQueryDTO query) {
        // 1.查 ES：分页 + 排序塞进 PageRequest，布尔查询圈定"匹配 + 过滤"
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> {
                    // 关键词全文匹配：must 参与打分；title^2 表示标题命中权重 x2（比描述命中更相关）
                    if (StringUtils.hasText(query.getKeyword())) {
                        b.must(m -> m.multiMatch(mm -> mm
                                .query(query.getKeyword())
                                .fields("title^2", "description")));
                    }
                    // filter 只筛选不打分（还能走 ES 缓存）：仅在售；分类有值才生效
                    b.filter(f -> f.term(t -> t.field("status").value(STATUS_ON_SALE)));
                    if (query.getCategoryId() != null) {
                        b.filter(f -> f.term(t -> t.field("categoryId").value(query.getCategoryId())));
                    }
                    return b;
                }))
                .withPageable(PageRequest.of(
                        (int) Math.max(query.getPageNum() - 1, 0),
                        (int) query.getPageSize(),
                        buildSearchSort(query.getSort())))
                .build();

        SearchHits<ProductDocument> hits = elasticsearchOperations.search(nativeQuery, ProductDocument.class);

        // 2.按命中顺序取出 id 列表（顺序即相关度/排序结果，后面不能打乱）
        List<Long> ids = hits.getSearchHits().stream()
                .map(hit -> hit.getContent().getId())
                .toList();
        if (ids.isEmpty()) {
            return PageResult.of(List.of(), hits.getTotalHits(), query.getPageNum(), query.getPageSize(), 0);
        }

        // 3.回 MySQL 补展示字段：批量查（避免 N+1），再按 ES 命中顺序重排
        Map<Long, Product> byId = productMapper.selectByIds(ids).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        List<Product> ordered = ids.stream()
                .map(byId::get)
                // 防御：ES 里有但 MySQL 已删的脏 id（双写接入前的过渡期），直接跳过
                .filter(Objects::nonNull)
                .toList();
        List<ProductVO> records = productConverter.toVOList(ordered, false);

        // 4.封装分页：总条数用 ES 的命中总数，页数自己算
        long total = hits.getTotalHits();
        long pages = (total + query.getPageSize() - 1) / query.getPageSize();
        return PageResult.of(records, total, query.getPageNum(), query.getPageSize(), pages);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public List<ProductVO> getRecommendedProducts() {
        // 在售商品，收藏数优先、浏览量次之倒序；Page(1, 8) 当 LIMIT 用，只取首页一排
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, STATUS_ON_SALE)
                .orderByDesc(Product::getFavoriteCount)
                .orderByDesc(Product::getViewCount);
        Page<Product> page = productMapper.selectPage(new Page<>(1, RECOMMEND_SIZE), wrapper);
        // 复用批量转 VO（一次性填充分类名/卖家名，避免 N+1）
        return productConverter.toVOList(page.getRecords(), false);
    }

    /**
     * 搜索排序：价格升/降、最新按时间倒序；不传 sort 时默认相关度（_score 降序）——ES 搜索的最大价值
     */
    private Sort buildSearchSort(String sort) {
        if ("priceAsc".equals(sort)) {
            return Sort.by(Sort.Direction.ASC, "price");
        }
        if ("priceDesc".equals(sort)) {
            return Sort.by(Sort.Direction.DESC, "price");
        }
        if ("latest".equals(sort)) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        // 不排序 = 按相关度 _score 降序，谁的标题/描述越匹配谁排前面
        return Sort.unsorted();
    }


    /**
     * 根据查询条件分页查询商品，并转成 VO 列表
     */
    @Nonnull
    private PageResult<ProductVO> getProductVOPageResult(long query, long query1, LambdaQueryWrapper<Product> wrapper) {
        Page<Product> page = productMapper.selectPage(
                new Page<>(query, query1), wrapper);

        // 3.批量转 VO（一次性填充分类名/卖家名，避免 N+1；自己的商品 favorite 恒为 false，此页不展示收藏态）
        List<ProductVO> records = productConverter.toVOList(page.getRecords(), false);

        // 4.封装分页结果
        return PageResult.of(records, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }


    /**
     * 根据 sort 参数决定排序方式，默认按发布时间倒序（最新）
     */
    private void applySort(LambdaQueryWrapper<Product> wrapper, String sort) {
        if ("priceAsc".equals(sort)) {
            wrapper.orderByAsc(Product::getPrice);
        } else if ("priceDesc".equals(sort)) {
            wrapper.orderByDesc(Product::getPrice);
        } else {
            wrapper.orderByDesc(Product::getCreatedAt);
        }
    }

    /**
     * 尽力同步 ES（upsert）：搜索是辅助能力，不能拖垮发布/编辑主链路。
     * ES 暂时不可用时只记 error 日志，事后可用 POST /es/reimport 全量重灌修复（幂等）。
     */
    private void syncToEsQuietly(Product product) {
        try {
            productSyncService.saveOne(product);
        } catch (Exception e) {
            log.error("商品同步 ES 失败，productId={}，请稍后调用 /es/reimport 修复", product.getId(), e);
        }
    }

    /**
     * 尽力同步 ES（删除）：同上，失败只记日志，靠 reimport 兜底
     */
    private void removeFromEsQuietly(Long productId) {
        try {
            productSyncService.removeOne(productId);
        } catch (Exception e) {
            log.error("商品删除 ES 文档失败，productId={}，请稍后调用 /es/reimport 修复", productId, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param query 分页 + 关键词参数
     * @return
     */
    @Override
    public PageResult<ProductVO> pageProductsForAdmin(AdminProductQueryDTO query) {
        // 后台看全部状态（不做"只在售"过滤），关键词命中标题，按发布时间倒序
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .like(StringUtils.hasText(query.getKeyword()), Product::getTitle, query.getKeyword())
                .orderByDesc(Product::getCreatedAt);

        Page<Product> page = productMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        // 复用批量转 VO（填充分类名/卖家名片；后台列表无登录用户语义，favorite 恒为 false）
        List<ProductVO> records = productConverter.toVOList(page.getRecords(), false);
        return PageResult.of(records, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    /**
     * {@inheritDoc}
     *
     * @param productId 商品ID
     * @param dto       状态更新参数
     * @return
     */
    @Override
    public ProductVO updateProductStatusByAdmin(Long productId, ProductStatusUpdateDTO dto) {
        // 1.状态白名单校验：只认 ON_SALE / OFF_SHELF / SOLD
        String status = dto.getStatus();
        if (!STATUS_ON_SALE.equals(status) && !STATUS_OFF_SHELF.equals(status) && !STATUS_SOLD.equals(status)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "非法的状态值");
        }

        // 2.查商品：不存在 → 404
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "商品不存在或已删除");
        }

        // 3.定向更新：只改 status 一列，不覆盖计数/时间
        productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .eq(Product::getId, productId)
                .set(Product::getStatus, status));

        // 4.双写 ES：上下架/售出后同步，保证搜索结果与 MySQL 一致
        product.setStatus(status);
        syncToEsQuietly(product);

        return productConverter.toVO(product, false);
    }

    /**
     * {@inheritDoc}
     *
     * @param productId 商品ID
     */
    @Override
    @Transactional
    public void deleteProductByAdmin(Long productId) {
        // 1.查商品：不存在 → 404
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "商品不存在或已删除");
        }

        // 2.先删该商品的所有收藏（无外键约束，需手动清理，避免孤儿收藏）
        favoriteService.removeByProductId(productId);

        // 3.再删商品本身（@Transactional 保证第 2、3 步同生共死）
        productMapper.deleteById(productId);

        // 4.双写 ES：删除搜索文档，避免搜到已删除的"幽灵商品"
        removeFromEsQuietly(productId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketStatsVO getMarketStats() {
        // 1.趋势窗口：近 14 天（含今天），与前端 Dashboard 图表的数据范围一致
        LocalDate today = LocalDate.now();
        LocalDateTime since = today.minusDays(TREND_DAYS - 1).atStartOfDay();

        // 2.商品总数 + 今日新增
        long productTotal = productMapper.selectCount(null);
        long todayNewProducts = productMapper.selectCount(
                new LambdaQueryWrapper<Product>().ge(Product::getCreatedAt, today.atStartOfDay()));

        // 3.按天聚合发布数并补 0，保证 14 天的轴连续（GROUP BY 只返回有数据的日期）
        Map<String, Long> countByDate = productMapper.countDailyPublishedSince(since).stream()
                .collect(Collectors.toMap(DailyCountDTO::getStatDate, DailyCountDTO::getCnt));
        List<TrendPointVO> productTrend = new ArrayList<>(TREND_DAYS);
        for (int i = 0; i < TREND_DAYS; i++) {
            String label = today.minusDays(TREND_DAYS - 1 - i)
                    .format(DateTimeFormatter.ofPattern("MM-dd"));
            productTrend.add(new TrendPointVO(label, countByDate.getOrDefault(label, 0L)));
        }

        MarketStatsVO vo = new MarketStatsVO();
        vo.setProductTotal(productTotal);
        vo.setTodayNewProducts(todayNewProducts);
        vo.setProductTrend(productTrend);
        return vo;
    }
}