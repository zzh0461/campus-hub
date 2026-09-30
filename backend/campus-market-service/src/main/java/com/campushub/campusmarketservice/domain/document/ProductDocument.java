package com.campushub.campusmarketservice.domain.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品在 Elasticsearch 里的文档结构（类比 MySQL 的表结构）
 * <p>
 * 只保留"搜索/过滤/排序"需要的字段；展示字段（图片、卖家名等）查询时回 MySQL 补齐，
 * 保证 MySQL 仍是唯一数据真相，ES 里不存会过期的副本。
 * <p>
 * 服务启动时 Spring Data ES 会按本类的注解自动创建索引 campus_product 及映射。
 *
 * @author CampusHub
 */
@Data
@Document(indexName = "campus_product")
// 单节点开发环境：副本数设 0，否则副本无处可放，集群健康永远是 yellow
@Setting(shards = 1, replicas = 0)
public class ProductDocument implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 商品ID：ES 文档主键，与 MySQL 主键同值，方便两边对齐 */
    @Id
    private Long id;

    /** 标题：全文搜索字段。存入用 ik_max_word 细切提高召回，搜索用 ik_smart 粗切提高精确 */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    /** 描述：同标题的分词策略 */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String description;

    /** 分类ID：只做精确过滤（eq），不分词，所以用 Keyword 家族里的数值类型 Long */
    @Field(type = FieldType.Long)
    private Long categoryId;

    /** 售价：用于价格区间过滤/排序；ES 里用 double 足够（展示精度回 MySQL 取 BigDecimal） */
    @Field(type = FieldType.Double)
    private Double price;

    /** 状态：ON_SALE/OFF_SHELF/SOLD，精确匹配不分词 → Keyword */
    @Field(type = FieldType.Keyword)
    private String status;

    /** 卖家ID：精确过滤用 */
    @Field(type = FieldType.Long)
    private Long sellerId;

    /** 收藏数：将来可作排序依据 */
    @Field(type = FieldType.Integer)
    private Integer favoriteCount;

    /** 浏览量：将来可作排序依据 */
    @Field(type = FieldType.Integer)
    private Integer viewCount;

    /**
     * 发布时间：按时间排序用
     * <p>
     * 用 date_hour_minute_second（即 yyyy-MM-dd'T'HH:mm:ss，到秒、无时区），对齐 MySQL datetime 精度：
     * LocalDateTime 不含时区偏移，若用 date_time（要求带偏移）写入会抛 UnsupportedTemporalTypeException
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createdAt;
}