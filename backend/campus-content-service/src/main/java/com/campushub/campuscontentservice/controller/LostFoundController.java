package com.campushub.campuscontentservice.controller;

import com.campushub.campuscontentservice.domain.dto.LostFoundPublishDTO;
import com.campushub.campuscontentservice.domain.dto.LostFoundQueryDTO;
import com.campushub.campuscontentservice.domain.dto.LostFoundStatusUpdateDTO;
import com.campushub.campuscontentservice.domain.vo.LostFoundVO;
import com.campushub.campuscontentservice.service.LostFoundService;
import com.campushub.common.context.UserContext;
import com.campushub.common.enums.ResultCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.response.PageResult;
import com.campushub.common.response.R;
import lombok.NonNull;
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
 * 失物招领控制器
 *
 * 路径在 /api/lost-found/** 下，网关已配好路由（强制 JWT 鉴权）。
 * 列表/详情是公共内容（发布者昵称靠 Feign 填充，无需当前用户）；
 * 发布/删除需登录，发布者ID 从 UserContext 取，不接受前端传，杜绝越权。
 *
 * @author CampusHub
 */
@RestController
@RequestMapping("/api/lost-found")
@RequiredArgsConstructor
public class LostFoundController {

    private final LostFoundService lostFoundService;

    /**
     * 失物招领分页列表：关键词/类型筛选，按发布时间倒序
     *
     * @param query 分页 + 筛选参数
     * @return 失物招领分页结果
     */
    @GetMapping
    public R<PageResult<LostFoundVO>> list(LostFoundQueryDTO query) {
        return R.success(lostFoundService.getLostFoundList(query));
    }

    /**
     * 我发布的失物招领（需登录）：仅返回当前登录用户自己发布的，可按关键词/类型筛选。
     * /my 是字面量路径，匹配优先于 /{id} 路径变量，不会被误当成 id="my" 的详情请求
     *
     * @param query 分页 + 筛选参数
     * @return 当前用户发布的失物招领分页结果
     */
    @GetMapping("/my")
    public R<PageResult<LostFoundVO>> my(LostFoundQueryDTO query) {
        // 发布者ID 从登录上下文取，不接受前端传，杜绝越权查他人发布
        Long publisherId = getUserId();
        return R.success(lostFoundService.getMyLostFoundList(publisherId, query));
    }

    /**
     * 失物招领详情
     *
     * 联系方式只对已登录用户开放：未登录时 contact 为空、contactVisible=false，
     * 前端据此显示"登录后查看联系方式"的引导（发布者填的是自由文本，无法脱敏）。
     *
     * @param id 记录ID
     * @return 详情（已填充发布者昵称/头像，联系方式按登录态裁剪）
     */
    @GetMapping("/{id}")
    public R<LostFoundVO> detail(@PathVariable Long id) {
        // 未登录时 UserContext 为空，这里不抛 401——详情本身是公开内容，只裁剪联系方式
        boolean loggedIn = UserContext.getUserId() != null;
        return R.success(lostFoundService.getLostFoundDetail(id, loggedIn));
    }

    /**
     * 发布失物/招领（需登录）
     *
     * @param dto 发布参数（@Validated 触发 DTO 上的校验注解）
     * @return 发布后的记录（前端拿 id 跳转详情页）
     */
    @PostMapping
    public R<LostFoundVO> publish(@RequestBody @Validated LostFoundPublishDTO dto) {
        // 发布者ID 从登录上下文取（网关校验 JWT 后注入），不接受前端传，杜绝越权挂别人名下
        Long publisherId = getUserId();
        return R.success(lostFoundService.publishLostFound(publisherId, dto));
    }

    /**
     * 更新自己发布的失物招领状态（需登录且必须是发布者本人）：
     * OPEN 进行中 ↔ RESOLVED 已解决（失物找回 / 招领被认领后标记）
     *
     * @param id  记录ID
     * @param dto 状态更新参数（@Validated 触发校验）
     * @return 更新后的记录
     */
    @PutMapping("/{id}/status")
    public R<LostFoundVO> updateStatus(@PathVariable Long id,
                                       @RequestBody @Validated LostFoundStatusUpdateDTO dto) {
        // 归属校验在 Service 内完成：非本人发布会抛 403
        Long userId = getUserId();
        return R.success(lostFoundService.updateLostFoundStatus(id, userId, dto));
    }

    /**
     * 删除自己发布的失物招领（需登录且必须是发布者本人）
     *
     * @param id 记录ID
     * @return 空响应体，成功即 code=200
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        // 归属校验在 Service 内完成：非本人发布会抛 403
        Long userId = getUserId();
        lostFoundService.deleteLostFound(id, userId);
        return R.success();
    }

    /**
     * 获取当前登录用户 ID，未登录抛 401
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
