package com.campushub.campusadminservice.controller;

import com.campushub.campusadminservice.client.ActivityAdminClient;
import com.campushub.campusadminservice.domain.dto.ActivitySaveDTO;
import com.campushub.campusadminservice.domain.dto.AdminPageQuery;
import com.campushub.campusadminservice.domain.vo.ActivityVO;
import com.campushub.common.response.PageResult;
import com.campushub.common.response.R;
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
 * 后台活动管理控制器
 *
 * 路径在 /api/admin/activities/** 下，网关强制 JWT 鉴权，
 * 且 AdminAuthInterceptor 已保证到这里的一定是 ADMIN 角色，故方法内无需再校验身份。
 *
 * admin 是聚合门面：自己不碰数据库，把请求透传给 activity-service 的 internal 管理接口，
 * 拿到结果后套 R 信封返回前端。
 *
 * @author CampusHub
 */
@RestController
@RequestMapping("/api/admin/activities")
@RequiredArgsConstructor
public class AdminActivityController {

    private final ActivityAdminClient activityAdminClient;

    /**
     * 活动分页列表：全部状态，关键词命中标题/介绍
     *
     * @param query 分页 + 关键词参数
     * @return 活动分页结果
     */
    @GetMapping
    public R<PageResult<ActivityVO>> list(AdminPageQuery query) {
        return R.success(activityAdminClient.page(
                query.getPageNum(), query.getPageSize(), query.getKeyword()));
    }

    /**
     * 新建活动：主办方由 activity-service 统一署名"平台管理员"
     *
     * @param dto 活动内容（@Validated 触发校验）
     * @return 新建后的活动
     */
    @PostMapping
    public R<ActivityVO> create(@RequestBody @Validated ActivitySaveDTO dto) {
        return R.success(activityAdminClient.create(dto));
    }

    /**
     * 编辑活动（标题/介绍/分类/地点/时间/名额/封面）
     *
     * @param id  活动ID
     * @param dto 活动内容
     * @return 更新后的活动
     */
    @PutMapping("/{id}")
    public R<ActivityVO> update(@PathVariable Long id,
                                @RequestBody @Validated ActivitySaveDTO dto) {
        return R.success(activityAdminClient.update(id, dto));
    }

    /**
     * 删除活动（连带删除报名记录）
     *
     * @param id 活动ID
     * @return 空响应体，成功即 code=200
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        activityAdminClient.delete(id);
        return R.success();
    }
}
