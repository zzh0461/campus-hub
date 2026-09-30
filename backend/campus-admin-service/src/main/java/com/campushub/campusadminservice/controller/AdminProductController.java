package com.campushub.campusadminservice.controller;

import com.campushub.campusadminservice.client.ProductAdminClient;
import com.campushub.campusadminservice.domain.dto.AdminPageQuery;
import com.campushub.campusadminservice.domain.dto.ProductStatusUpdateDTO;
import com.campushub.campusadminservice.domain.vo.ProductVO;
import com.campushub.common.response.PageResult;
import com.campushub.common.response.R;
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
 * 后台商品管理控制器
 *
 * 路径在 /api/admin/products/** 下，网关强制 JWT 鉴权，
 * 且 AdminAuthInterceptor 已保证到这里的一定是 ADMIN 角色，故方法内无需再校验身份。
 *
 * admin 是聚合门面：自己不碰数据库，把请求透传给 market-service 的 internal 管理接口，
 * 拿到结果后套 R 信封返回前端。
 *
 * @author CampusHub
 */
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductAdminClient productAdminClient;

    /**
     * 商品分页列表：全部状态（在售/下架/已售出），关键词命中标题
     *
     * @param query 分页 + 关键词参数
     * @return 商品分页结果
     */
    @GetMapping
    public R<PageResult<ProductVO>> list(AdminPageQuery query) {
        return R.success(productAdminClient.page(
                query.getPageNum(), query.getPageSize(), query.getKeyword()));
    }

    /**
     * 更新商品状态（上架/下架/标记售出），卖家归属校验由 market-service 内部接口跳过
     *
     * @param id  商品ID
     * @param dto 状态更新参数（@Validated 触发校验）
     * @return 更新后的商品
     */
    @PutMapping("/{id}/status")
    public R<ProductVO> updateStatus(@PathVariable Long id,
                                     @RequestBody @Validated ProductStatusUpdateDTO dto) {
        return R.success(productAdminClient.updateStatus(id, dto));
    }

    /**
     * 删除商品（连带清理收藏记录与 ES 文档）
     *
     * @param id 商品ID
     * @return 空响应体，成功即 code=200
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        productAdminClient.delete(id);
        return R.success();
    }
}
