package com.campushub.campusadminservice.client;

import com.campushub.campusadminservice.domain.dto.AnnouncementSaveDTO;
import com.campushub.campusadminservice.domain.vo.AnnouncementVO;
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
 * 公告管理 Feign 客户端
 *
 * 调用 content-service 的 /internal/admin/announcements 内部接口，把公告管理能力"借"给后台。
 *
 * contextId 必须显式指定：同服务的 LostFoundAdminClient 与本客户端 name 相同，
 * 而 OpenFeign 按服务名注册 FeignClientSpecification，重名会启动报错；
 * contextId 只隔离 Bean 命名空间，不影响 Nacos 服务发现（仍按 name 找服务）。
 *
 * 与 UserAdminClient 同一哲学：刻意不设 fallback，后台管理场景要"响亮地失败"——
 * content-service 不可用时让异常冒泡，由全局处理器返回错误，前端提示操作失败；
 * 静默降级会让管理员误判（公告没删掉以为删掉了），比直接报错更危险。
 *
 * @author CampusHub
 */
@FeignClient(name = "campus-content-service", contextId = "announcementAdminClient",
        path = "/internal/admin/announcements")
public interface AnnouncementAdminClient {

    /**
     * 分页查询全部公告（含未发布）
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param keyword  关键词（标题/内容），null 时不筛选
     * @return 公告分页结果
     */
    @GetMapping
    PageResult<AnnouncementVO> page(@RequestParam("pageNum") long pageNum,
                                    @RequestParam("pageSize") long pageSize,
                                    @RequestParam("keyword") String keyword);

    /**
     * 新建公告
     *
     * @param dto 公告内容
     * @return 新建后的公告
     */
    @PostMapping
    AnnouncementVO create(@RequestBody AnnouncementSaveDTO dto);

    /**
     * 编辑公告
     *
     * @param id  公告ID
     * @param dto 公告内容
     * @return 更新后的公告
     */
    @PutMapping("/{id}")
    AnnouncementVO update(@PathVariable("id") Long id, @RequestBody AnnouncementSaveDTO dto);

    /**
     * 删除公告
     *
     * @param id 公告ID
     */
    @DeleteMapping("/{id}")
    void delete(@PathVariable("id") Long id);
}
