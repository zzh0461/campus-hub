package com.campushub.campusmarketservice.repository;

import com.campushub.campusmarketservice.domain.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 商品 ES 仓库（类比 MyBatis 的 Mapper，但面向 Elasticsearch）
 * <p>
 * 继承 ElasticsearchRepository 即免费获得 save / saveAll / deleteById / search 等能力，
 * 本步先用内置方法完成全量导入与增删同步。
 *
 * @author CampusHub
 */
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Long> {
}
