package com.campushub.campusactivityservice.controller;

import com.campushub.campusactivityservice.domain.dto.ActivityQueryDTO;
import com.campushub.campusactivityservice.domain.dto.ActivitySaveDTO;
import com.campushub.campusactivityservice.domain.dto.MyActivityQueryDTO;
import com.campushub.campusactivityservice.domain.vo.ActivityCategoryVO;
import com.campushub.campusactivityservice.domain.vo.ActivityVO;
import com.campushub.campusactivityservice.service.ActivityService;
import com.campushub.common.context.UserContext;
import com.campushub.common.enums.ResultCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.response.PageResult;
import com.campushub.common.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 活动控制器
 *
 * 路径在 /api/activities/** 下，网关已配好路由且不在白名单（强制 JWT 鉴权）。
 * 用户身份统一从 UserContext 获取（网关校验 JWT 后透传的 X-User-Id 填充）
 *
 * @author CampusHub
 */
@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    /**
     * 活动分类列表（筛选下拉框数据源，按排序值升序）
     *
     * @return 分类列表
     */
    @GetMapping("/categories")
    public R<List<ActivityCategoryVO>> categories() {
        return R.success(activityService.getCategories());
    }

    /**
     * 活动分页列表：关键词/分类/状态筛选，未开始的活动排前面
     *
     * @param query 分页 + 筛选参数
     * @return 活动分页结果
     */
    @GetMapping
    public R<PageResult<ActivityVO>> list(ActivityQueryDTO query) {
        return R.success(activityService.getActivities(getUserId(), query));
    }

    /**
     * 首页"即将开始"：UPCOMING 状态取前几条（非分页）。
     * 注意 /upcoming 是字面量路径，Spring MVC 匹配时字面量优先于 /{id} 路径变量，
     * 不会被误当成 id="upcoming" 的详情请求（同 market 的 /products/recommend）
     *
     * @return 即将开始的活动列表
     */
    @GetMapping("/upcoming")
    public R<List<ActivityVO>> upcoming() {
        return R.success(activityService.getUpcomingActivities(getUserId()));
    }

    /**
     * 我的活动：当前用户报名过的活动，分页（报名时间倒序）
     *
     * @param query 分页参数
     * @return 活动分页结果
     */
    @GetMapping("/my")
    public R<PageResult<ActivityVO>> my(MyActivityQueryDTO query) {
        return R.success(activityService.getMyActivities(getUserId(), query));
    }

    /**
     * 我发布的活动：当前用户作为主办方创建的活动，分页（创建时间倒序）
     *
     * @param query 分页参数
     * @return 活动分页结果
     */
    @GetMapping("/my-published")
    public R<PageResult<ActivityVO>> myPublished(MyActivityQueryDTO query) {
        return R.success(activityService.getMyPublishedActivities(getUserId(), query));
    }

    /**
     * 发布活动：任何人（含普通用户）都可以办活动，主办方=自己，报名通知会发给自己。
     * 字面量路径 /my、/my-published 与 /{id} 不冲突（匹配更具体的优先）
     *
     * @param dto 活动内容（@Validated 触发校验）
     * @return 发布后的活动详情
     */
    @PostMapping
    public R<ActivityVO> publish(@RequestBody @Validated ActivitySaveDTO dto) {
        return R.success(activityService.publishActivity(getUserId(), dto));
    }

    /**
     * 编辑自己发布的活动：仅发布者本人可改（403 保护），返回更新后的详情
     *
     * @param id  活动ID
     * @param dto 活动内容
     * @return 更新后的活动详情
     */
    @PutMapping("/{id}")
    public R<ActivityVO> update(@PathVariable Long id, @RequestBody @Validated ActivitySaveDTO dto) {
        return R.success(activityService.updateMyActivity(getUserId(), id, dto));
    }

    /**
     * 删除自己发布的活动：仅发布者本人可删；已有人报名时被拦（400），
     * 提示先与报名者沟通——后台删除不受此限
     *
     * @param id 活动ID
     * @return 空响应体，成功即 code=200
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        activityService.deleteMyActivity(getUserId(), id);
        return R.success();
    }

    /**
     * 活动详情：含分类名与当前用户是否已报名（registered）
     *
     * @param id 活动ID
     * @return 活动详情
     */
    @GetMapping("/{id}")
    public R<ActivityVO> detail(@PathVariable Long id) {
        return R.success(activityService.getActivityDetail(id, getUserId()));
    }

    /**
     * 报名活动：成功后返回最新详情（registered=true、名额已扣）。
     * POST 与下面的 DELETE 共用 /{id}/registrations 路径，靠 HTTP 方法区分（报名/取消语义相反）；
     * 与 /{id} 也不冲突——多一个 /registrations 段，匹配更具体的优先
     *
     * @param id 活动ID
     * @return 报名后的活动详情
     */
    @PostMapping("/{id}/registrations")
    public R<ActivityVO> register(@PathVariable Long id) {
        return R.success(activityService.register(getUserId(), id));
    }

    /**
     * 取消报名：成功后返回最新详情（registered=false、名额已回补）
     *
     * @param id 活动ID
     * @return 取消后的活动详情
     */
    @DeleteMapping("/{id}/registrations")
    public R<ActivityVO> cancelRegistration(@PathVariable Long id) {
        return R.success(activityService.cancelRegistration(getUserId(), id));
    }

    /**
     * 获取当前登录用户ID，未登录则抛 401
     */
    @NonNull
    private static Long getUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }
}
