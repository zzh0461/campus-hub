package com.campushub.campusadminservice.client;

import com.campushub.campusadminservice.domain.dto.LostFoundStatusUpdateDTO;
import com.campushub.campusadminservice.domain.vo.ContentStatsVO;
import com.campushub.campusadminservice.domain.vo.LostFoundVO;
import com.campushub.common.response.PageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 失物招领管理 Feign 客户端
 *
 * 调用 content-service 的 /internal/admin/lost-found 内部接口，把失物管理能力"借"给后台。
 * contextId 用于与同服务的 AnnouncementAdminClient 隔离 Bean 命名空间（同名 name 会冲突）。
 * 刻意不设 fallback，理由同 UserAdminClient：后台管理要"响亮地失败"，静默降级比报错更危险。
 *
 * @author CampusHub
 */
@FeignClient(name = "campus-content-service", contextId = "lostFoundAdminClient",
        path = "/internal/admin/lost-found")
public interface LostFoundAdminClient {

    /**
     * 分页查询全部失物招领
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param keyword  关键词（标题/描述），null 时不筛选
     * @return 失物招领分页结果
     */
    @GetMapping
    PageResult<LostFoundVO> page(@RequestParam("pageNum") long pageNum,
                                 @RequestParam("pageSize") long pageSize,
                                 @RequestParam("keyword") String keyword);

    /**
     * 后台改状态（OPEN ↔ RESOLVED）
     *
     * @param id  记录ID
     * @param dto 状态更新参数
     * @return 更新后的记录
     */
    @PutMapping("/{id}/status")
    LostFoundVO updateStatus(@PathVariable("id") Long id, @RequestBody LostFoundStatusUpdateDTO dto);

    /**
     * 后台删除任意失物招领记录
     *
     * @param id 记录ID
     */
    @DeleteMapping("/{id}")
    void delete(@PathVariable("id") Long id);

    /**
     * 失物招领统计：总数 + 失物/招领分类计数（供 Dashboard 聚合）
     *
     * @return 内容统计
     */
    @GetMapping("/stats")
    ContentStatsVO stats();
}
