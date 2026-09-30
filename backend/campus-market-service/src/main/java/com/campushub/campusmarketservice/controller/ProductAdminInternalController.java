package com.campushub.campusmarketservice.controller;

import com.campushub.campusmarketservice.domain.dto.AdminProductQueryDTO;
import com.campushub.campusmarketservice.domain.dto.ProductStatusUpdateDTO;
import com.campushub.campusmarketservice.domain.vo.MarketStatsVO;
import com.campushub.campusmarketservice.domain.vo.ProductVO;
import com.campushub.campusmarketservice.service.ProductService;
import com.campushub.common.response.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品后台管理内部接口控制器
 *
 * 路径以 /internal 开头：网关未配置该前缀的路由，外部无法通过网关(8080)访问，
 * 仅供 admin-service 通过 OpenFeign 服务间调用，把商品管理能力"借"给后台。
 * 直接返回 PageResult / ProductVO / MarketStatsVO，不套 R 信封：
 * R 是"只出不进"的（私有构造 + final 字段），Feign 无法反序列化。
 *
 * 与用户侧 ProductController 的区别：后台能看全部状态、能改任意商品状态、能删任意商品。
 *
 * @author CampusHub
 */
@RestController
@RequestMapping("/internal/admin/products")
@RequiredArgsConstructor
public class ProductAdminInternalController {

    private final ProductService productService;

    /**
     * 分页查询全部商品（含下架/已售出），关键词命中标题
     *
     * @param query 分页 + 关键词参数，由查询串自动绑定
     * @return 商品分页结果
     */
    @GetMapping
    public PageResult<ProductVO> page(AdminProductQueryDTO query) {
        return productService.pageProductsForAdmin(query);
    }

    /**
     * 后台更新商品状态（上架/下架/标记售出），无需卖家归属校验
     *
     * @param id  商品ID
     * @param dto 状态更新参数（@Validated 触发校验）
     * @return 更新后的商品
     */
    @PutMapping("/{id}/status")
    public ProductVO updateStatus(@PathVariable Long id,
                                  @RequestBody @Validated ProductStatusUpdateDTO dto) {
        return productService.updateProductStatusByAdmin(id, dto);
    }

    /**
     * 后台删除任意商品（连带清理收藏记录与 ES 文档）
     *
     * @param id 商品ID
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        productService.deleteProductByAdmin(id);
    }

    /**
     * 市场统计：总数 + 今日新增 + 近 14 天发布趋势（供 Dashboard）
     *
     * @return 市场统计
     */
    @GetMapping("/stats")
    public MarketStatsVO stats() {
        return productService.getMarketStats();
    }
}
