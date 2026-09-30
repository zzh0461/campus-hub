package com.campushub.campusmarketservice.controller;

import com.campushub.campusmarketservice.domain.dto.MyProductQueryDTO;
import com.campushub.campusmarketservice.domain.dto.ProductPublishDTO;
import com.campushub.campusmarketservice.domain.dto.ProductQueryDTO;
import com.campushub.campusmarketservice.domain.dto.ProductUpdateDTO;
import com.campushub.campusmarketservice.domain.vo.CategoryVO;
import com.campushub.campusmarketservice.domain.vo.ProductVO;
import com.campushub.campusmarketservice.service.ProductService;
import com.campushub.campusmarketservice.service.ProductSyncService;
import com.campushub.common.context.UserContext;
import com.campushub.common.enums.ResultCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.response.PageResult;
import com.campushub.common.response.R;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品控制器（对外公开接口）
 * <p>
 * 路径在 /api/market/** 下，由网关路由到本服务，需 JWT 鉴权。
 *
 * @author CampusHub
 */
@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductSyncService productSyncService;

    /**
     * 商品分页列表
     *
     * @param query 查询参数（Spring 自动从 URL 查询串绑定）
     * @return 在售商品分页结果
     */
    @GetMapping("/products")
    public R<PageResult<ProductVO>> list(ProductQueryDTO query) {
        return R.success(productService.getProducts(query));
    }

    /**
     * 商品详情
     *
     * @param id 商品ID（路径变量，来自 /api/market/products/{id}）
     * @return 商品详情（浏览量已 +1，favorite 按当前用户计算）
     */
    @GetMapping("/products/{id}")
    public R<ProductVO> detail(@PathVariable("id") Long id) {
        // 市场详情需登录，userId 一般非空；万一为空，Service 内部会当作"未收藏"处理
        Long userId = UserContext.getUserId();
        return R.success(productService.getProductDetail(id, userId));
    }

    /**
     * 商品分类列表（供前端筛选下拉框使用）
     *
     * @return 全部分类，按排序值升序
     */
    @GetMapping("/categories")
    public R<List<CategoryVO>> categories() {
        return R.success(productService.getCategories());
    }

    /**
     * 发布商品（需登录）
     *
     * @param dto 发布参数（@Validated 触发 DTO 上的校验注解）
     * @return 发布后的商品（前端拿 id 跳转详情页）
     */
    @PostMapping("/products")
    public R<ProductVO> publish(@RequestBody @Validated ProductPublishDTO dto) {
        // 卖家ID 从登录上下文取（网关校验 JWT 后注入），不接受前端传，杜绝越权挂别人名下
        Long sellerId = getUserId();
        return R.success(productService.publishProduct(sellerId, dto));
    }

    /**
     * 我的商品（需登录）：仅返回当前登录卖家自己发布的商品，可按状态筛选
     *
     * @param query 分页 + 状态筛选参数
     * @return 当前用户的商品分页结果
     */
    @GetMapping("/products/my")
    public R<PageResult<ProductVO>> myProducts(MyProductQueryDTO query) {
        // 卖家ID 从登录上下文取，不接受前端传，杜绝越权查他人商品
        Long sellerId = getUserId();
        return R.success(productService.getMyProducts(sellerId, query));
    }

    /**
     * 更新商品（编辑内容 / 上下架，需登录且必须是卖家本人）
     *
     * @param id  商品ID（路径变量）
     * @param dto 更新参数（status 可选）
     * @return 更新后的商品
     */
    @PutMapping("/products/{id}")
    public R<ProductVO> update(@PathVariable("id") Long id, @RequestBody @Validated ProductUpdateDTO dto) {
        Long userId = getUserId();
        return R.success(productService.updateProduct(id, userId, dto));
    }

    /**
     * 删除商品（需登录且必须是卖家本人）
     *
     * @param id 商品ID（路径变量）
     * @return 空响应体，成功即 code=200
     */
    @DeleteMapping("/products/{id}")
    public R<Void> delete(@PathVariable("id") Long id) {
        // 归属校验在 Service 内完成：非本人商品会抛 403
        Long userId = getUserId();
        productService.deleteProduct(id, userId);
        return R.success();
    }

    /**
     * 运维端点：全量重建商品 ES 索引数据（首次建索引 / ES 重建后对账用）
     * <p>
     * 幂等可重复执行；直连 8084 调用即可，无需登录态。
     *
     * @return 导入条数
     */
    @PostMapping("/es/reimport")
    public R<Integer> reimportToEs() {
        return R.success(productSyncService.importAll());
    }

    /**
     * 商品搜索（ES 全文匹配）：前端搜索框入口
     * <p>
     * 与 /products 的区别：关键词走 ES 分词 + 相关度打分，而不是 MySQL LIKE。
     *
     * @param query 搜索参数（keyword/分类/排序/分页）
     * @return 在售商品分页结果（按相关度或指定排序）
     */
    @GetMapping("/search")
    public R<PageResult<ProductVO>> search(ProductQueryDTO query) {
        return R.success(productService.searchProducts(query));
    }

    /**
     * 首页热门推荐：在售商品按收藏数/浏览量倒序取前 8
     * <p>
     * 路由说明：/products/recommend 是字面量路径，Spring MVC 匹配优先级高于
     * /products/{id} 的路径变量，不会把 "recommend" 当成 id 捕获。
     *
     * @return 推荐商品列表（非分页，前端直接遍历渲染）
     */
    @GetMapping("/products/recommend")
    public R<List<ProductVO>> recommend() {
        return R.success(productService.getRecommendedProducts());
    }

    /**
     * 获取当前登录用户 ID，未登录抛 401
     */
    @NonNull
    private static Long getUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }
}