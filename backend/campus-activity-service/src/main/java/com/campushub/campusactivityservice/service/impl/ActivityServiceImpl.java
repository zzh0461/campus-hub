package com.campushub.campusactivityservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campushub.campusactivityservice.config.RabbitConfig;
import com.campushub.campusactivityservice.domain.dto.ActivityQueryDTO;
import com.campushub.campusactivityservice.domain.dto.ActivitySaveDTO;
import com.campushub.campusactivityservice.domain.dto.AdminActivityQueryDTO;
import com.campushub.campusactivityservice.domain.dto.DailyCountDTO;
import com.campushub.campusactivityservice.domain.dto.MyActivityQueryDTO;
import com.campushub.campusactivityservice.domain.entity.Activity;
import com.campushub.campusactivityservice.domain.entity.ActivityCategory;
import com.campushub.campusactivityservice.domain.entity.ActivityRegistration;
import com.campushub.campusactivityservice.domain.event.ActivityRegisteredEvent;
import com.campushub.campusactivityservice.domain.vo.ActivityCategoryVO;
import com.campushub.campusactivityservice.domain.vo.ActivityStatsVO;
import com.campushub.campusactivityservice.domain.vo.ActivityVO;
import com.campushub.campusactivityservice.domain.vo.TrendPointVO;
import com.campushub.campusactivityservice.domain.vo.UserBriefVO;
import com.campushub.campusactivityservice.client.UserClient;
import com.campushub.campusactivityservice.mapper.ActivityCategoryMapper;
import com.campushub.campusactivityservice.mapper.ActivityMapper;
import com.campushub.campusactivityservice.mapper.ActivityRegistrationMapper;
import com.campushub.campusactivityservice.service.ActivityService;
import com.campushub.common.enums.ResultCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.response.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 活动服务实现类
 *
 * @author CampusHub
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    /** 首页"即将开始"展示条数：一排卡片 6 个够用 */
    private static final int UPCOMING_LIMIT = 6;

    /** 未开始状态：upcoming 接口只查这个状态的活动 */
    private static final String STATUS_UPCOMING = "UPCOMING";

    /** 进行中状态：后台创建/编辑活动时按时间推算 */
    private static final String STATUS_ONGOING = "ONGOING";

    /** 已结束状态：后台创建/编辑活动时按时间推算 */
    private static final String STATUS_FINISHED = "FINISHED";

    /** 后台创建活动的主办方署名：与公告"平台管理员"署名同一惯例 */
    private static final String PLATFORM_ORGANIZER = "平台管理员";

    /** Dashboard 趋势图天数：近 14 天（含今天），与前端图表数据范围一致 */
    private static final int TREND_DAYS = 14;

    private final ActivityMapper activityMapper;
    private final ActivityCategoryMapper activityCategoryMapper;
    private final ActivityRegistrationMapper activityRegistrationMapper;
    private final RabbitTemplate rabbitTemplate;
    private final UserClient userClient;

    @Override
    public List<ActivityCategoryVO> getCategories() {
        // 全量小表，按排序值升序查出后转 VO（sortOrder 只用于排序，不暴露给前端）
        List<ActivityCategory> categories = activityCategoryMapper.selectList(
                new LambdaQueryWrapper<ActivityCategory>()
                        .orderByAsc(ActivityCategory::getSortOrder));
        return categories.stream().map(c -> {
            ActivityCategoryVO vo = new ActivityCategoryVO();
            vo.setId(c.getId());
            vo.setName(c.getName());
            return vo;
        }).toList();
    }

    @Override
    public PageResult<ActivityVO> getActivities(Long userId, ActivityQueryDTO query) {
        // 1.组装查询条件：全部筛选可选
        //   关键词用 and(...or...) 包住：避免 OR 泄漏破坏其他 AND 条件
        //   （等价 SQL: AND (title LIKE ? OR description LIKE ?)，括号缺一不可）
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<Activity>()
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(Activity::getTitle, query.getKeyword())
                        .or()
                        .like(Activity::getDescription, query.getKeyword()))
                .eq(query.getCategoryId() != null, Activity::getCategoryId, query.getCategoryId())
                .eq(StringUtils.hasText(query.getStatus()), Activity::getStatus, query.getStatus())
                // 2.排序对齐市场：默认最新发布在前（createdAt 倒序）——
                //   全站列表统一"最新的在前面"，刚发布的活动立刻可见；
                //   "最快开场优先"的语义化排序只保留在首页 upcoming 接口
                .orderByDesc(Activity::getCreatedAt);

        Page<Activity> page = activityMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        List<Activity> records = page.getRecords();
        if (records.isEmpty()) {
            return PageResult.of(List.of(), page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
        }

        // 3.批量填充分类名与报名态（与 upcoming/详情/我的活动共用同一套转换链路）
        List<ActivityVO> vos = toVOList(records, userId);

        // 4.封装统一分页结构
        return PageResult.of(vos, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    @Override
    public List<ActivityVO> getUpcomingActivities(Long userId) {
        // UPCOMING 按开始时间升序 = 最快开场的排前面；Page(1, 6) 当 LIMIT 用（同 market 推荐接口）
        Page<Activity> page = activityMapper.selectPage(
                new Page<>(1, UPCOMING_LIMIT),
                new LambdaQueryWrapper<Activity>()
                        .eq(Activity::getStatus, STATUS_UPCOMING)
                        .orderByAsc(Activity::getStartTime));
        return toVOList(page.getRecords(), userId);
    }

    @Override
    public ActivityVO getActivityDetail(Long id, Long userId) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        // 单条也走批量转换链路：多一次小查询换一套代码，不值得为它单写转换逻辑
        return toVOList(List.of(activity), userId).get(0);
    }

    @Override
    public PageResult<ActivityVO> getMyActivities(Long userId, MyActivityQueryDTO query) {
        // 1.第一步：分页查报名记录（报名时间倒序，最新报名的排前面）
        Page<ActivityRegistration> regPage = activityRegistrationMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()),
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getUserId, userId)
                        .orderByDesc(ActivityRegistration::getCreatedAt));
        List<ActivityRegistration> registrations = regPage.getRecords();
        if (registrations.isEmpty()) {
            return PageResult.of(List.of(), regPage.getTotal(), query.getPageNum(), query.getPageSize(), 0);
        }

        // 2.第二步：按报名顺序批量查活动（selectByIds 不保证返回顺序，要自己按 id 列表重排；
        //   活动可能已被删除，filter 掉查不到的，同 market 我的收藏）
        List<Long> activityIds = registrations.stream().map(ActivityRegistration::getActivityId).toList();
        Map<Long, Activity> byId = activityMapper.selectByIds(activityIds).stream()
                .collect(Collectors.toMap(Activity::getId, Function.identity()));
        List<Activity> ordered = activityIds.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .toList();

        // 3.转 VO：仍走公共链路（registered 会再查一次报名表，这里必然全部 true——
        //   多一次小查询换零重复代码，划算）
        List<ActivityVO> vos = toVOList(ordered, userId);

        // 4.封装分页：total/pages 以报名记录数为准
        return PageResult.of(vos, regPage.getTotal(), query.getPageNum(), query.getPageSize(), regPage.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivityVO register(Long userId, Long activityId) {
        // 1.查活动，不存在抛 404
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        // 2.状态校验：只有未开始的活动能报名（进行中/已结束都拦下）
        if (!STATUS_UPCOMING.equals(activity.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "活动已开始或已结束，无法报名");
        }
        // 3.幂等：已报名直接拦（快速失败，省去无谓的扣名额）
        if (isRegistered(userId, activityId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "你已报名该活动");
        }
        // 4.名额守卫（并发防超卖的核心）：原子 +1，WHERE 带 current < max。
        //   “判断还有名额”与“扣名额”在同一条 SQL 里完成，没有“先查再改”的时间窗：
        //   100 人抢最后 1 个名额，数据库行锁保证只有 1 个请求 updated=1，其余 updated=0 被拦
        int updated = activityMapper.update(null,
                new LambdaUpdateWrapper<Activity>()
                        .setSql("current_participants = current_participants + 1")
                        .eq(Activity::getId, activityId)
                        // 列与列比较（current < max）LambdaWrapper 没有现成方法，用 apply 拼原生条件
                        .apply("current_participants < max_participants"));
        if (updated == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "活动名额已满");
        }
        // 5.插报名记录（createdAt 由数据库 DEFAULT 填充）。uk_activity_user 唯一约束是最后防线：
        //   极端并发下同一用户两个请求同时过了第 3 步，第二个 insert 撞唯一键抛异常，
        //   @Transactional 把第 4 步的 +1 一起回滚，名额不会被白占
        ActivityRegistration registration = new ActivityRegistration();
        registration.setActivityId(activityId);
        registration.setUserId(userId);
        activityRegistrationMapper.insert(registration);

        // 6.事务提交后再发 MQ 通知组织者：不能在事务内直接发——
        //   若发消息成功但事务随后回滚（如第 5 步唯一键冲突），组织者会收到一条“有人报名”的假通知。
        //   registerSynchronization 的 afterCommit 回调保证“库里真落定了才发”。
        //   自己报名自己组织的活动不发；organizerId 为空也不发（种子数据里主办方未必是平台用户）
        Long organizerId = activity.getOrganizerId();
        if (organizerId != null && !organizerId.equals(userId)) {
            // 第 4 步的 +1 发生在 SQL 里，内存 activity 还是旧值，手动 +1 得最新报名人数
            int latestCount = activity.getCurrentParticipants() + 1;
            String activityTitle = activity.getTitle();
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishRegisteredEventQuietly(organizerId, activityTitle, userId, latestCount);
                }
            });
        }

        // 7.返回最新详情（registered=true、剩余名额已刷新），前端拿来直接更新 UI 无需重拉列表
        return getActivityDetail(activityId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivityVO cancelRegistration(Long userId, Long activityId) {
        // 1.删报名记录：WHERE 带 activityId + userId（归属校验融进 SQL，只能删自己的）
        int deleted = activityRegistrationMapper.delete(
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getActivityId, activityId)
                        .eq(ActivityRegistration::getUserId, userId));
        // 2.没删掉 = 本来就没报名，直接告知（也避免把名额减成负数）
        if (deleted == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "你尚未报名该活动");
        }
        // 3.名额原子 -1：WHERE 带 current > 0 双保险，防止并发下减成负数
        activityMapper.update(null,
                new LambdaUpdateWrapper<Activity>()
                        .setSql("current_participants = current_participants - 1")
                        .eq(Activity::getId, activityId)
                        .gt(Activity::getCurrentParticipants, 0));
        // 4.返回最新详情（registered=false、名额已回补）
        return getActivityDetail(activityId, userId);
    }

    /**
     * {@inheritDoc}
     *
     * @param query 分页 + 关键词参数
     * @return
     */
    @Override
    public PageResult<ActivityVO> pageActivitiesForAdmin(AdminActivityQueryDTO query) {
        // 1.后台看全部状态，关键词命中"标题 OR 介绍"（and(...) 包住 OR，防条件泄漏），
        //   管理列表按创建时间倒序（最新创建的排前面）
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<Activity>()
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(Activity::getTitle, query.getKeyword())
                        .or()
                        .like(Activity::getDescription, query.getKeyword()))
                .orderByDesc(Activity::getCreatedAt);

        Page<Activity> page = activityMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        // 2.转 VO：userId 传 null → 无报名态语义，registered 恒为 false
        List<ActivityVO> vos = toVOList(page.getRecords(), null);

        // 3.封装统一分页结构
        return PageResult.of(vos, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    /**
     * {@inheritDoc}
     *
     * @param dto 活动内容
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivityVO createActivityForAdmin(ActivitySaveDTO dto) {
        // 1.业务校验：时间窗必须正着走、分类必须真实存在（跨表无外键，靠代码维护一致性）
        validateTimeWindow(dto);
        validateCategoryExists(dto.getCategoryId());

        // 2.DTO → 实体：主办方用后台指定的名称（未填则退回"平台管理员"默认署名）；
        //   organizerId 不设（null）：报名通知逻辑对 organizerId=null 的活动自动跳过，
        //   不会发给不存在的对象；current_participants 由数据库默认 0 填充
        Activity activity = new Activity();
        BeanUtils.copyProperties(dto, activity);
        activity.setOrganizer(resolveOrganizer(dto.getOrganizer()));
        activity.setOrganizerId(null);
        activity.setStatus(resolveStatus(dto.getStartTime(), dto.getEndTime()));

        // 3.入库：id/createdAt/updatedAt 由数据库填充
        activityMapper.insert(activity);

        // 4.回查转 VO 返回，前端弹窗关闭后刷新列表即见新活动
        return toVOList(List.of(activityMapper.selectById(activity.getId())), null).get(0);
    }

    /**
     * {@inheritDoc}
     *
     * @param id  活动ID
     * @param dto 活动内容
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivityVO updateActivityForAdmin(Long id, ActivitySaveDTO dto) {
        // 1.查活动：不存在 → 404
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 2.业务校验：时间窗、分类；名额不允许改到已报名人数以下（否则已报名的人"凭空超员"）
        validateTimeWindow(dto);
        validateCategoryExists(dto.getCategoryId());
        if (dto.getMaxParticipants() < activity.getCurrentParticipants()) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "人数上限不能小于当前已报名人数（" + activity.getCurrentParticipants() + "人）");
        }

        // 3.定向更新：状态按新时间重新推算；描述/封面允许清空（set 显式覆盖 null）；
        //   主办方仅在后台明确填写时才覆盖，避免"没填"把原有署名清掉
        activityMapper.update(null, new LambdaUpdateWrapper<Activity>()
                .eq(Activity::getId, id)
                .set(Activity::getTitle, dto.getTitle())
                .set(Activity::getDescription, dto.getDescription())
                .set(Activity::getCategoryId, dto.getCategoryId())
                .set(Activity::getLocation, dto.getLocation())
                .set(Activity::getCover, dto.getCover())
                .set(Activity::getStartTime, dto.getStartTime())
                .set(Activity::getEndTime, dto.getEndTime())
                .set(Activity::getMaxParticipants, dto.getMaxParticipants())
                .set(Activity::getStatus, resolveStatus(dto.getStartTime(), dto.getEndTime()))
                .set(StringUtils.hasText(dto.getOrganizer()), Activity::getOrganizer, dto.getOrganizer()));

        // 4.回查转 VO 返回
        return toVOList(List.of(activityMapper.selectById(id)), null).get(0);
    }

    /**
     * {@inheritDoc}
     *
     * @param id 活动ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteActivityForAdmin(Long id) {
        // 1.查活动：不存在 → 404
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 2.先删报名记录再删活动（无外键约束，手动清理避免孤儿报名；
        //   @Transactional 保证两步同生共死）
        activityRegistrationMapper.delete(
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getActivityId, id));
        activityMapper.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActivityStatsVO getActivityStats() {
        // 1.趋势窗口：近 14 天（含今天），与前端 Dashboard 图表的数据范围一致
        LocalDate today = LocalDate.now();
        LocalDateTime since = today.minusDays(TREND_DAYS - 1).atStartOfDay();

        // 2.活动总数 + 今日报名数
        long activityTotal = activityMapper.selectCount(null);
        long todayRegistrations = activityRegistrationMapper.selectCount(
                new LambdaQueryWrapper<ActivityRegistration>()
                        .ge(ActivityRegistration::getCreatedAt, today.atStartOfDay()));

        // 3.按天聚合报名数并补 0，保证 14 天的轴连续（GROUP BY 只返回有数据的日期）
        Map<String, Long> countByDate = activityRegistrationMapper.countDailyRegisteredSince(since).stream()
                .collect(Collectors.toMap(DailyCountDTO::getStatDate, DailyCountDTO::getCnt));
        List<TrendPointVO> registrationTrend = new ArrayList<>(TREND_DAYS);
        for (int i = 0; i < TREND_DAYS; i++) {
            String label = today.minusDays(TREND_DAYS - 1 - i)
                    .format(DateTimeFormatter.ofPattern("MM-dd"));
            registrationTrend.add(new TrendPointVO(label, countByDate.getOrDefault(label, 0L)));
        }

        ActivityStatsVO vo = new ActivityStatsVO();
        vo.setActivityTotal(activityTotal);
        vo.setTodayRegistrations(todayRegistrations);
        vo.setRegistrationTrend(registrationTrend);
        return vo;
    }

    /**
     * {@inheritDoc}
     *
     * @param userId 发布者用户ID
     * @param dto    活动内容
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivityVO publishActivity(Long userId, ActivitySaveDTO dto) {
        // 1.业务校验：时间窗、分类（与后台新建同一套规则）
        validateTimeWindow(dto);
        validateCategoryExists(dto.getCategoryId());

        // 2.DTO → 实体：主办方优先用用户填写的名称（替社团/组织办活动的场景），
        //   未填则取发布者昵称（跨服务查 user；降级拿不到时退回通用署名）；
        //   organizerId=发布者ID——之后有人报名，MQ 通知会准确送达发布者
        Activity activity = new Activity();
        BeanUtils.copyProperties(dto, activity);
        activity.setOrganizer(StringUtils.hasText(dto.getOrganizer())
                ? dto.getOrganizer().trim()
                : resolveUserNickname(userId));
        activity.setOrganizerId(userId);
        activity.setStatus(resolveStatus(dto.getStartTime(), dto.getEndTime()));

        // 3.入库：id/createdAt/updatedAt、current_participants 由数据库填充
        activityMapper.insert(activity);

        // 4.回查转 VO 返回（带上报名态——发布者自己 registered 必为 false）
        return toVOList(List.of(activityMapper.selectById(activity.getId())), userId).get(0);
    }

    /**
     * {@inheritDoc}
     *
     * @param userId 当前登录用户ID
     * @param query  分页参数
     * @return
     */
    @Override
    public PageResult<ActivityVO> getMyPublishedActivities(Long userId, MyActivityQueryDTO query) {
        // 只看 organizerId = 自己的活动，创建时间倒序（最新发布的排前面）
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<Activity>()
                .eq(Activity::getOrganizerId, userId)
                .orderByDesc(Activity::getCreatedAt);

        Page<Activity> page = activityMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        // 转 VO 时 userId 传当前用户：列表里的 registered 表示"我是否报名了自己发布的活动"，语义正确
        List<ActivityVO> vos = toVOList(page.getRecords(), userId);
        return PageResult.of(vos, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    /**
     * {@inheritDoc}
     *
     * @param userId 当前登录用户ID
     * @param id     活动ID
     * @param dto    活动内容
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivityVO updateMyActivity(Long userId, Long id, ActivitySaveDTO dto) {
        // 1.查活动：不存在 → 404
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 2.归属校验：只有发布者本人能改，否则 → 403（后台内部接口不受此限）
        if (!userId.equals(activity.getOrganizerId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能编辑自己发布的活动");
        }

        // 3.业务校验：时间窗、分类、名额不许改到已报名人数以下
        validateTimeWindow(dto);
        validateCategoryExists(dto.getCategoryId());
        if (dto.getMaxParticipants() < activity.getCurrentParticipants()) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "人数上限不能小于当前已报名人数（" + activity.getCurrentParticipants() + "人）");
        }

        // 4.定向更新：organizerId（身份）不允许改，organizer（署名文本）可改；
        //   状态按新时间重新推算
        activityMapper.update(null, new LambdaUpdateWrapper<Activity>()
                .eq(Activity::getId, id)
                .set(Activity::getTitle, dto.getTitle())
                .set(Activity::getDescription, dto.getDescription())
                .set(Activity::getCategoryId, dto.getCategoryId())
                .set(Activity::getLocation, dto.getLocation())
                .set(Activity::getCover, dto.getCover())
                .set(Activity::getStartTime, dto.getStartTime())
                .set(Activity::getEndTime, dto.getEndTime())
                .set(Activity::getMaxParticipants, dto.getMaxParticipants())
                .set(Activity::getStatus, resolveStatus(dto.getStartTime(), dto.getEndTime()))
                .set(StringUtils.hasText(dto.getOrganizer()), Activity::getOrganizer, dto.getOrganizer().trim()));

        // 5.回查转 VO 返回
        return toVOList(List.of(activityMapper.selectById(id)), userId).get(0);
    }

    /**
     * {@inheritDoc}
     *
     * @param userId 当前登录用户ID
     * @param id     活动ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMyActivity(Long userId, Long id) {
        // 1.查活动：不存在 → 404
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 2.归属校验：只有发布者本人能删，否则 → 403
        if (!userId.equals(activity.getOrganizerId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能删除自己发布的活动");
        }

        // 3.已有人报名时不允许删：报名者的日程/通知都挂在这个活动上，不能说没就没
        //   （后台删除不受此限——管理员处理违规内容时连带报名一起清）
        if (activity.getCurrentParticipants() != null && activity.getCurrentParticipants() > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "已有 " + activity.getCurrentParticipants() + " 人报名，不能删除");
        }

        // 4.删除（此时无报名记录，无需清理；保险起见仍按活动ID兜底删一遍报名）
        activityRegistrationMapper.delete(
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getActivityId, id));
        activityMapper.deleteById(id);
    }

    /**
     * 查用户昵称（跨服务）：查不到/user-service 降级时退回通用署名，
     * 保证 organizer 非空（表里该列 NOT NULL）
     */
    private String resolveUserNickname(Long userId) {
        return userClient.listByIds(List.of(userId)).stream()
                .findFirst()
                .map(UserBriefVO::getNickname)
                .filter(StringUtils::hasText)
                .orElse(PLATFORM_ORGANIZER);
    }

    /**
     * 后台活动的主办方署名：后台明确填写时用填写值，未填退回"平台管理员"默认署名
     */
    private String resolveOrganizer(String organizer) {
        return StringUtils.hasText(organizer) ? organizer : PLATFORM_ORGANIZER;
    }

    /**
     * 时间窗校验：结束时间必须晚于开始时间
     */
    private void validateTimeWindow(ActivitySaveDTO dto) {
        if (!dto.getEndTime().isAfter(dto.getStartTime())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "结束时间必须晚于开始时间");
        }
    }

    /**
     * 分类存在性校验：活动表与分类表无外键，靠代码挡住不存在的分类ID
     */
    private void validateCategoryExists(Long categoryId) {
        if (activityCategoryMapper.selectById(categoryId) == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "活动分类不存在");
        }
    }

    /**
     * 按起止时间与当前时刻推算活动状态（后台创建/编辑没有人工选状态的入口，状态只能算出来）
     */
    private String resolveStatus(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(start)) {
            return STATUS_UPCOMING;
        }
        if (now.isAfter(end)) {
            return STATUS_FINISHED;
        }
        return STATUS_ONGOING;
    }

    /**
     * 当前用户是否已报名该活动（统计报名记录数 > 0）
     */
    private boolean isRegistered(Long userId, Long activityId) {
        Long count = activityRegistrationMapper.selectCount(
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getUserId, userId)
                        .eq(ActivityRegistration::getActivityId, activityId));
        return count != null && count > 0;
    }

    /**
     * 发布活动报名事件（Quietly 版）：与 market 收藏事件同一哲学——
     * MQ 挂了/网络抖动不能让报名操作失败，只记 error 日志。
     * 代价：通知可能丢一条（非关键数据，可接受）；关键业务才需要事务消息/本地消息表
     */
    private void publishRegisteredEventQuietly(Long organizerId, String activityTitle,
                                               Long actorId, Integer currentParticipants) {
        try {
            ActivityRegisteredEvent event = new ActivityRegisteredEvent(
                    organizerId, activityTitle, actorId, currentParticipants);
            rabbitTemplate.convertAndSend(
                    RabbitConfig.NOTIFICATION_EXCHANGE,
                    RabbitConfig.ROUTING_ACTIVITY_REGISTER,
                    event);
        } catch (Exception e) {
            log.error("发布活动报名事件失败，activityTitle={}，通知将缺失但报名已成功", activityTitle, e);
        }
    }

    /**
     * 实体列表 → VO 列表：一次性批量填充分类名与当前用户报名态，避免 N+1
     * （列表/upcoming/详情/我的活动四个接口共用，同 market 的 ProductConverter.toVOList 思想）
     *
     * userId 为 null 表示无"当前用户"语义（后台列表），跳过报名态查询、registered 恒为 false
     */
    private List<ActivityVO> toVOList(List<Activity> activities, Long userId) {
        if (activities.isEmpty()) {
            return List.of();
        }

        // 批量查分类名：一次 IN 查询建 Map，转 VO 时按 id 取
        Set<Long> categoryIds = activities.stream()
                .map(Activity::getCategoryId)
                .collect(Collectors.toSet());
        Map<Long, String> categoryNames = activityCategoryMapper.selectByIds(categoryIds).stream()
                .collect(Collectors.toMap(ActivityCategory::getId, ActivityCategory::getName));

        // 批量查当前用户对这批活动的报名记录：只 select activity_id 一列，建成 Set 供 O(1) 判断
        Set<Long> registeredIds = Set.of();
        if (userId != null) {
            List<Long> activityIds = activities.stream().map(Activity::getId).toList();
            registeredIds = activityRegistrationMapper.selectList(
                            new LambdaQueryWrapper<ActivityRegistration>()
                                    .select(ActivityRegistration::getActivityId)
                                    .eq(ActivityRegistration::getUserId, userId)
                                    .in(ActivityRegistration::getActivityId, activityIds)).stream()
                    .map(ActivityRegistration::getActivityId)
                    .collect(Collectors.toSet());
        }

        Set<Long> finalRegisteredIds = registeredIds;
        return activities.stream()
                .map(a -> toVO(a, categoryNames.get(a.getCategoryId()), finalRegisteredIds.contains(a.getId())))
                .toList();
    }

    /**
     * 实体 → VO：计算字段（remainingParticipants/registered）在这里填充
     */
    private ActivityVO toVO(Activity a, String categoryName, boolean registered) {
        ActivityVO vo = new ActivityVO();
        vo.setId(a.getId());
        vo.setTitle(a.getTitle());
        vo.setDescription(a.getDescription());
        vo.setCategoryId(a.getCategoryId());
        vo.setCategoryName(categoryName);
        vo.setLocation(a.getLocation());
        vo.setCover(a.getCover());
        vo.setStartTime(a.getStartTime());
        vo.setEndTime(a.getEndTime());
        vo.setMaxParticipants(a.getMaxParticipants());
        vo.setCurrentParticipants(a.getCurrentParticipants());
        // 剩余名额兜底 0：并发下 current 可能短暂超过 max，不能给前端负数
        vo.setRemainingParticipants(Math.max(0, a.getMaxParticipants() - a.getCurrentParticipants()));
        vo.setRegistered(registered);
        vo.setOrganizer(a.getOrganizer());
        vo.setOrganizerId(a.getOrganizerId());
        vo.setStatus(a.getStatus());
        vo.setCreatedAt(a.getCreatedAt());
        return vo;
    }
}
