package com.campushub.campuscontentservice.service;

import com.campushub.campuscontentservice.domain.dto.LostFoundPublishDTO;
import com.campushub.campuscontentservice.domain.dto.LostFoundQueryDTO;
import com.campushub.campuscontentservice.domain.dto.LostFoundStatusUpdateDTO;
import com.campushub.campuscontentservice.domain.vo.ContentStatsVO;
import com.campushub.campuscontentservice.domain.vo.LostFoundVO;
import com.campushub.common.response.PageResult;

/**
 * 失物招领服务接口
 *
 * @author CampusHub
 */
public interface LostFoundService {

    /**
     * 分页查询失物招领（关键词/类型筛选），按发布时间倒序
     *
     * 列表接口不下发联系方式（contact 置空、contactVisible=false）：
     * 列表页本来就不展示联系方式，没必要把发布者的电话塞进批量响应里被随意抓取，
     * 需要联系时走详情页 + 登录校验。
     *
     * @param query 分页 + 筛选参数
     * @return 失物招领分页结果（已填充发布者昵称/头像，联系方式已裁剪）
     */
    PageResult<LostFoundVO> getLostFoundList(LostFoundQueryDTO query);

    /**
     * 分页查询"我发布的"失物招领（关键词/类型筛选），按发布时间倒序
     *
     * @param publisherId 当前登录用户ID（发布者，由后端注入，不接受前端传）
     * @param query       分页 + 筛选参数
     * @return 当前用户发布的失物招领分页结果
     */
    PageResult<LostFoundVO> getMyLostFoundList(Long publisherId, LostFoundQueryDTO query);

    /**
     * 失物招领详情；不存在抛 404
     *
     * 联系方式（contact，发布者自行填写的自由文本）按隐私口径只对已登录用户开放：
     * 未登录时 contact 置空、contactVisible=false，前端据此引导登录。
     *
     * @param id              记录ID
     * @param contactVisible  当前请求者是否已登录（true=可下发联系方式）
     * @return 详情（已填充发布者昵称/头像，联系方式按登录态裁剪）
     */
    LostFoundVO getLostFoundDetail(Long id, boolean contactVisible);

    /**
     * 发布失物/招领：发布者从登录上下文注入，状态默认 OPEN
     *
     * @param publisherId 当前登录用户ID（发布者）
     * @param dto         发布参数
     * @return 发布后的记录（含 id，前端跳详情用）
     */
    LostFoundVO publishLostFound(Long publisherId, LostFoundPublishDTO dto);

    /**
     * 更新自己发布的失物招领状态（OPEN / RESOLVED）：非本人抛 403，状态值非法抛 400
     *
     * @param id     记录ID
     * @param userId 当前登录用户ID（发布者）
     * @param dto    状态更新参数
     * @return 更新后的记录
     */
    LostFoundVO updateLostFoundStatus(Long id, Long userId, LostFoundStatusUpdateDTO dto);

    /**
     * 删除自己发布的失物招领：非本人抛 403，不存在抛 404
     *
     * @param id     记录ID
     * @param userId 当前登录用户ID
     */
    void deleteLostFound(Long id, Long userId);

    /**
     * 后台改失物招领状态（OPEN ↔ RESOLVED）：无需发布者归属校验（管理员可处理任意人的记录）
     *
     * 状态值白名单 OPEN / RESOLVED；记录不存在抛 404。
     *
     * @param id  记录ID
     * @param dto 状态更新参数
     * @return 更新后的记录（含发布者名片）
     */
    LostFoundVO updateLostFoundStatusByAdmin(Long id, LostFoundStatusUpdateDTO dto);

    /**
     * 后台删除任意失物招领记录：无需归属校验；不存在抛 404
     *
     * @param id 记录ID
     */
    void deleteLostFoundByAdmin(Long id);

    /**
     * 失物招领统计（供 admin-service 的 Dashboard 聚合）：记录总数 + 失物/招领分类计数
     *
     * @return 内容统计
     */
    ContentStatsVO getContentStats();
}
