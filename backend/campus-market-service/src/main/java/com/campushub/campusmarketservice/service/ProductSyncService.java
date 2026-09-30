package com.campushub.campusmarketservice.service;

import com.campushub.campusmarketservice.domain.document.ProductDocument;
import com.campushub.campusmarketservice.domain.entity.Product;
import com.campushub.campusmarketservice.mapper.ProductMapper;
import com.campushub.campusmarketservice.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品 MySQL ↔ ES 同步服务
 * <p>
 * 职责单一：只管"数据怎么进/出 ES"。第 3 步的全量导入、第 5 步发布/编辑/删除时的双写，都走这里。
 * 不做接口+实现拆分：它是运维/基础设施性质的工具服务，不存在第二种实现。
 *
 * @author CampusHub
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSyncService {

    private final ProductMapper productMapper;
    private final ProductSearchRepository productSearchRepository;

    /**
     * 全量导入：把 MySQL 全部商品转成 ES 文档批量写入
     * <p>
     * 幂等：文档 id 与 MySQL 主键同值，重复导入即覆盖，不会重复堆积。
     *
     * @return 导入条数
     */
    public int importAll() {
        List<Product> products = productMapper.selectList(null);
        List<ProductDocument> docs = products.stream().map(this::toDocument).toList();
        productSearchRepository.saveAll(docs);
        log.info("商品全量导入 ES 完成，共 {} 条", docs.size());
        return docs.size();
    }

    /**
     * 增量同步：upsert 单条商品（发布/编辑/上下架后调用）
     */
    public void saveOne(Product product) {
        productSearchRepository.save(toDocument(product));
    }

    /**
     * 增量同步：删除单条商品文档（商品删除后调用）
     */
    public void removeOne(Long productId) {
        productSearchRepository.deleteById(productId);
    }

    /**
     * MySQL 实体 → ES 文档：price 由 BigDecimal 转 double（ES 数值类型），其余同名拷贝
     */
    private ProductDocument toDocument(Product product) {
        ProductDocument doc = new ProductDocument();
        doc.setId(product.getId());
        doc.setTitle(product.getTitle());
        doc.setDescription(product.getDescription());
        doc.setCategoryId(product.getCategoryId());
        doc.setPrice(product.getPrice() == null ? null : product.getPrice().doubleValue());
        doc.setStatus(product.getStatus());
        doc.setSellerId(product.getSellerId());
        doc.setFavoriteCount(product.getFavoriteCount());
        doc.setViewCount(product.getViewCount());
        doc.setCreatedAt(product.getCreatedAt());
        return doc;
    }
}