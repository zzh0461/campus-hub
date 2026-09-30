package com.campushub.campusactivityservice.service;

import com.campushub.campusactivityservice.domain.dto.ActivityQueryDTO;
import com.campushub.campusactivityservice.domain.dto.ActivitySaveDTO;
import com.campushub.campusactivityservice.domain.dto.AdminActivityQueryDTO;
import com.campushub.campusactivityservice.domain.dto.MyActivityQueryDTO;
import com.campushub.campusactivityservice.domain.vo.ActivityCategoryVO;
import com.campushub.campusactivityservice.domain.vo.ActivityStatsVO;
import com.campushub.campusactivityservice.domain.vo.ActivityVO;
import com.campushub.common.response.PageResult;

import java.util.List;

/**
 * 活动服务接口
 *
 * @author CampusHub
 */
public interface ActivityService {

    /**
     * 查询全部活动分类（按排序值升序，供前端筛选下拉框使用）
     *
     * @return 分类列表
     */
    List<ActivityCategoryVO> getCategories();

    /**
     * 分页查询活动（关键词/分类/状态筛选）：
     * 未开始在前、同状态按开始时间升序；批量填充分类名与当前用户报名态
     *
     * @param userId 当前登录用户ID（用于计算 registered，网关强制鉴权后必有值）
     * @param query  查询参数
     * @return 活动分页结果
     */
    PageResult<ActivityVO> getActivities(Long userId, ActivityQueryDTO query);

    /**
     * 首页"即将开始"：UPCOMING 状态按开始时间升序取前几条（非分页）
     *
     * @param userId 当前登录用户ID（用于计算 registered）
     * @return 即将开始的活动列表
     */
    List<ActivityVO> getUpcomingActivities(Long userId);

    /**
     * 活动详情：含分类名与当前用户报名态；活动不存在抛 404
     *
     * @param id     活动ID
     * @param userId 当前登录用户ID（用于计算 registered）
     * @return 活动详情 VO
     */
    ActivityVO getActivityDetail(Long id, Long userId);

    /**
     * 我的活动：分页查当前用户的报名记录（报名时间倒序），再批量查活动并按报名顺序重排
     * （两步查分页：与 market "我的收藏"同款套路）
     *
     * @param userId 当前登录用户ID
     * @param query  分页参数
     * @return 活动分页结果（total 以报名记录数为准）
     */
    PageResult<ActivityVO> getMyActivities(Long userId, MyActivityQueryDTO query);

    /**
     * 报名活动：名额守卫用原子 SQL（UPDATE ... WHERE current < max）防并发超卖，
     * 插报名记录与扣名额在同一事务内，同生共死。
     * 活动不存在抛 404；非未开始/已报名/名额已满抛 400（带自定义提示）
     *
     * @param userId     当前登录用户ID
     * @param activityId 活动ID
     * @return 报名后的最新活动详情（registered=true、剩余名额已刷新）
     */
    ActivityVO register(Long userId, Long activityId);

    /**
     * 取消报名：DELETE 的 WHERE 带 activityId + userId（归属校验融进 SQL，只能删自己的），
     * 确实删掉记录才原子回补名额（WHERE current > 0 双保险防负数）。
     * 未报名抛 400
     *
     * @param userId     当前登录用户ID
     * @param activityId 活动ID
     * @return 取消后的最新活动详情（registered=false、名额已回补）
     */
    ActivityVO cancelRegistration(Long userId, Long activityId);

    /**
     * 后台分页查询活动（供 admin-service 调用）：全部状态，关键词命中标题/介绍
     *
     * 后台无"当前用户"语义，VO 的 registered 恒为 false。
     *
     * @param query 分页 + 关键词参数
     * @return 活动分页结果
     */
    PageResult<ActivityVO> pageActivitiesForAdmin(AdminActivityQueryDTO query);

    /**
     * 后台新建活动：主办方统一署名"平台管理员"，
     * 状态按起止时间与当前时刻推算（未开始/进行中/已结束）
     *
     * 分类不存在、结束时间不晚于开始时间抛 400。
     *
     * @param dto 活动内容（@Validated 触发校验）
     * @return 新建后的活动
     */
    ActivityVO createActivityForAdmin(ActivitySaveDTO dto);

    /**
     * 后台编辑活动（标题/介绍/分类/地点/时间/名额/封面），状态按新时间重新推算
     *
     * 活动不存在抛 404；名额不能小于当前已报名人数、结束时间不晚于开始时间抛 400。
     *
     * @param id  活动ID
     * @param dto 活动内容
     * @return 更新后的活动
     */
    ActivityVO updateActivityForAdmin(Long id, ActivitySaveDTO dto);

    /**
     * 后台删除活动：连带删除该活动的全部报名记录
     *
     * @param id 活动ID
     */
    void deleteActivityForAdmin(Long id);

    /**
     * 后台活动统计（供 admin-service 的 Dashboard 聚合）
     *
     * 活动总数、今日报名数、近 14 天报名趋势（日期轴连续，无报名的日期补 0）。
     *
     * @return 活动统计
     */
    ActivityStatsVO getActivityStats();

    /**
     * 用户发布活动：organizer 取发布者昵称（跨服务查 user），organizerId = 发布者ID，
     * 状态按起止时间推算。结束时间不晚于开始时间、分类不存在抛 400
     *
     * @param userId 发布者用户ID（由后端注入，不接受前端传）
     * @param dto    活动内容（@Validated 触发校验）
     * @return 发布后的活动
     */
    ActivityVO publishActivity(Long userId, ActivitySaveDTO dto);

    /**
     * 我发布的活动：organizerId = 当前用户的活动，按创建时间倒序
     *
     * @param userId 当前登录用户ID
     * @param query  分页参数
     * @return 活动分页结果
     */
    PageResult<ActivityVO> getMyPublishedActivities(Long userId, MyActivityQueryDTO query);

    /**
     * 用户编辑自己发布的活动：仅 organizerId = 当前用户可改，否则抛 403。
     * 状态按新时间重新推算；名额不能小于当前已报名人数、结束时间不晚于开始时间抛 400
     *
     * @param userId 当前登录用户ID
     * @param id     活动ID
     * @param dto    活动内容
     * @return 更新后的活动
     */
    ActivityVO updateMyActivity(Long userId, Long id, ActivitySaveDTO dto);

    /**
     * 用户删除自己发布的活动：仅 organizerId = 当前用户可删，否则抛 403。
     * 已有人报名时不允许删（保护报名者），提示先处理名额；连带删除报名记录
     *
     * @param userId 当前登录用户ID
     * @param id     活动ID
     */
    void deleteMyActivity(Long userId, Long id);
}
