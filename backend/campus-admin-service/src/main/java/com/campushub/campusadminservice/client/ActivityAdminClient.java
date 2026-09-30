package com.campushub.campusadminservice.client;

import com.campushub.campusadminservice.domain.dto.ActivitySaveDTO;
import com.campushub.campusadminservice.domain.vo.ActivityStatsVO;
import com.campushub.campusadminservice.domain.vo.ActivityVO;
import com.campushub.common.response.PageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 活动管理 Feign 客户端
 *
 * 调用 activity-service 的 /internal/admin/activities 内部接口，把活动管理能力"借"给后台。
 * 刻意不设 fallback，理由同 UserAdminClient：后台管理要"响亮地失败"，静默降级比报错更危险。
 *
 * @author CampusHub
 */
@FeignClient(name = "campus-activity-service", path = "/internal/admin/activities")
public interface ActivityAdminClient {

    /**
     * 分页查询全部活动
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param keyword  关键词（标题/介绍），null 时不筛选
     * @return 活动分页结果
     */
    @GetMapping
    PageResult<ActivityVO> page(@RequestParam("pageNum") long pageNum,
                                @RequestParam("pageSize") long pageSize,
                                @RequestParam("keyword") String keyword);

    /**
     * 新建活动
     *
     * @param dto 活动内容
     * @return 新建后的活动
     */
    @PostMapping
    ActivityVO create(@RequestBody ActivitySaveDTO dto);

    /**
     * 编辑活动
     *
     * @param id  活动ID
     * @param dto 活动内容
     * @return 更新后的活动
     */
    @PutMapping("/{id}")
    ActivityVO update(@PathVariable("id") Long id, @RequestBody ActivitySaveDTO dto);

    /**
     * 删除活动
     *
     * @param id 活动ID
     */
    @DeleteMapping("/{id}")
    void delete(@PathVariable("id") Long id);

    /**
     * 活动统计：总数 + 今日报名 + 近 14 天报名趋势（供 Dashboard 聚合）
     *
     * @return 活动统计
     */
    @GetMapping("/stats")
    ActivityStatsVO stats();
}
