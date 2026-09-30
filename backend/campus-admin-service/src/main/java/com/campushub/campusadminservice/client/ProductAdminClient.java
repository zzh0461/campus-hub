package com.campushub.campusadminservice.client;

import com.campushub.campusadminservice.domain.dto.ProductStatusUpdateDTO;
import com.campushub.campusadminservice.domain.vo.MarketStatsVO;
import com.campushub.campusadminservice.domain.vo.ProductVO;
import com.campushub.common.response.PageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 商品管理 Feign 客户端
 *
 * 调用 market-service 的 /internal/admin/products 内部接口，把商品管理能力"借"给后台。
 * 刻意不设 fallback，理由同 UserAdminClient：后台管理要"响亮地失败"，静默降级比报错更危险。
 *
 * @author CampusHub
 */
@FeignClient(name = "campus-market-service", path = "/internal/admin/products")
public interface ProductAdminClient {

    /**
     * 分页查询全部商品（含下架/已售出）
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param keyword  关键词（标题），null 时不筛选
     * @return 商品分页结果
     */
    @GetMapping
    PageResult<ProductVO> page(@RequestParam("pageNum") long pageNum,
                               @RequestParam("pageSize") long pageSize,
                               @RequestParam("keyword") String keyword);

    /**
     * 后台更新商品状态（上架/下架/标记售出）
     *
     * @param id  商品ID
     * @param dto 状态更新参数
     * @return 更新后的商品
     */
    @PutMapping("/{id}/status")
    ProductVO updateStatus(@PathVariable("id") Long id, @RequestBody ProductStatusUpdateDTO dto);

    /**
     * 后台删除任意商品
     *
     * @param id 商品ID
     */
    @DeleteMapping("/{id}")
    void delete(@PathVariable("id") Long id);

    /**
     * 市场统计：总数 + 今日新增 + 近 14 天发布趋势（供 Dashboard 聚合）
     *
     * @return 市场统计
     */
    @GetMapping("/stats")
    MarketStatsVO stats();
}
