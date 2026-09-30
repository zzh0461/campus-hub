package com.campushub.campusactivityservice.controller;

import com.campushub.campusactivityservice.domain.dto.ActivitySaveDTO;
import com.campushub.campusactivityservice.domain.dto.AdminActivityQueryDTO;
import com.campushub.campusactivityservice.domain.vo.ActivityStatsVO;
import com.campushub.campusactivityservice.domain.vo.ActivityVO;
import com.campushub.campusactivityservice.service.ActivityService;
import com.campushub.common.response.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 活动后台管理内部接口控制器
 *
 * 路径以 /internal 开头：网关未配置该前缀的路由，外部无法通过网关(8080)访问，
 * 仅供 admin-service 通过 OpenFeign 服务间调用，把活动管理能力"借"给后台。
 * 直接返回 PageResult / ActivityVO / ActivityStatsVO，不套 R 信封：
 * R 是"只出不进"的（私有构造 + final 字段），Feign 无法反序列化。
 *
 * 与用户侧 ActivityController 的区别：后台能看全部状态、能新建/编辑/删除活动；
 * 后台无"当前用户"语义，VO 的 registered 恒为 false。
 *
 * @author CampusHub
 */
@RestController
@RequestMapping("/internal/admin/activities")
@RequiredArgsConstructor
public class ActivityAdminInternalController {

    private final ActivityService activityService;

    /**
     * 分页查询全部活动（全部状态），关键词命中标题/介绍
     *
     * @param query 分页 + 关键词参数，由查询串自动绑定
     * @return 活动分页结果
     */
    @GetMapping
    public PageResult<ActivityVO> page(AdminActivityQueryDTO query) {
        return activityService.pageActivitiesForAdmin(query);
    }

    /**
     * 新建活动：主办方统一署名"平台管理员"，状态按起止时间推算
     *
     * @param dto 活动内容（@Validated 触发校验）
     * @return 新建后的活动
     */
    @PostMapping
    public ActivityVO create(@RequestBody @Validated ActivitySaveDTO dto) {
        return activityService.createActivityForAdmin(dto);
    }

    /**
     * 编辑活动（标题/介绍/分类/地点/时间/名额/封面），状态按新时间重新推算
     *
     * @param id  活动ID
     * @param dto 活动内容
     * @return 更新后的活动
     */
    @PutMapping("/{id}")
    public ActivityVO update(@PathVariable Long id, @RequestBody @Validated ActivitySaveDTO dto) {
        return activityService.updateActivityForAdmin(id, dto);
    }

    /**
     * 删除活动（连带删除报名记录）
     *
     * @param id 活动ID
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        activityService.deleteActivityForAdmin(id);
    }

    /**
     * 活动统计：总数 + 今日报名 + 近 14 天报名趋势（供 Dashboard 聚合）
     *
     * @return 活动统计
     */
    @GetMapping("/stats")
    public ActivityStatsVO stats() {
        return activityService.getActivityStats();
    }
}
