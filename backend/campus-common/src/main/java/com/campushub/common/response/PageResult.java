package com.campushub.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 通用分页响应结果
 * <p>
 * 所有分页接口统一返回该结构,序列化后的 JSON 格式:
 * <pre>
 * {
 *   "records": [...],   // 当前页数据列表
 *   "total": 100,       // 总记录数
 *   "pageNum": 1,       // 当前页码
 *   "pageSize": 10,     // 每页条数
 *   "pages": 10         // 总页数
 * }
 * </pre>
 * 通常配合 {@link R#success(Object)} 使用:
 * {@code return R.success(PageResult.of(records, total, pageNum, pageSize, pages));}
 * </p>
 * <p>
 * 分页请求参数统一使用 {@code pageNum} / {@code pageSize}
 * </p>
 *
 * @param <T> 列表元素类型,如商品、活动、公告等实体
 */
@Data
@Schema(description = "通用分页响应结果")
public class PageResult<T> {

    /** 当前页数据列表(空列表由无参构造器兜底,不会为 null) */
    @Schema(description = "当前页数据列表")
    private List<T> records;

    /** 满足查询条件的总记录数,前端据此渲染分页组件总条数 */
    @Schema(description = "总记录数", example = "100")
    private long total;

    /** 当前页码,从 1 开始 */
    @Schema(description = "当前页码", example = "1")
    private long pageNum;

    /** 每页显示条数 */
    @Schema(description = "每页大小", example = "10")
    private long pageSize;

    /** 总页数 = ceil(total / pageSize) */
    @Schema(description = "总页数", example = "10")
    private long pages;

    /**
     * 无参构造器:records 默认空列表
     * <p>
     * 供 Jackson 反序列化或需要逐字段 setter 赋值的场景使用。
     * </p>
     */
    public PageResult() {
        this.records = Collections.emptyList();
    }

    /**
     * 全参构造器
     *
     * @param records  当前页数据列表
     * @param total    总记录数
     * @param pageNum  当前页码
     * @param pageSize 每页条数
     * @param pages    总页数
     */
    public PageResult(List<T> records,
                      long total,
                      long pageNum,
                      long pageSize,
                      long pages) {

        this.records = records;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = pages;
    }

    /**
     * 静态工厂:构建分页结果
     *
     * @param records  当前页数据列表
     * @param total    总记录数
     * @param pageNum  当前页码
     * @param pageSize 每页条数
     * @param pages    总页数
     * @param <T>      列表元素类型
     * @return 分页结果对象
     */
    public static <T> PageResult<T> of(
            List<T> records,
            long total,
            long pageNum,
            long pageSize,
            long pages) {

        return new PageResult<>(
                records,
                total,
                pageNum,
                pageSize,
                pages
        );
    }
}