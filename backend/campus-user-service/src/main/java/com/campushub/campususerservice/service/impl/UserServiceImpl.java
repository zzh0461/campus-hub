package com.campushub.campususerservice.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campushub.campususerservice.domain.dto.AdminUserQueryDTO;
import com.campushub.campususerservice.domain.dto.DailyCountDTO;
import com.campushub.campususerservice.domain.dto.UserDTO;
import com.campushub.campususerservice.domain.dto.UserStatusUpdateDTO;
import com.campushub.campususerservice.domain.entity.User;
import com.campushub.campususerservice.domain.vo.TrendPointVO;
import com.campushub.campususerservice.domain.vo.UserBriefVO;
import com.campushub.campususerservice.domain.vo.UserPublicVO;
import com.campushub.campususerservice.domain.vo.UserStatsVO;
import com.campushub.campususerservice.domain.vo.UserVO;
import com.campushub.campususerservice.mapper.UserMapper;
import com.campushub.campususerservice.service.UserService;
import com.campushub.common.context.UserContext;
import com.campushub.common.enums.ResultCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.response.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 *
 * @author CampusHub
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    /** 正常状态：可登录 */
    private static final String STATUS_ACTIVE = "ACTIVE";

    /** 禁用状态：封禁后无法登录 */
    private static final String STATUS_DISABLED = "DISABLED";

    /** 管理员角色：受保护，不允许被后台禁用或删除，避免把管理员锁死在系统外 */
    private static final String ROLE_ADMIN = "ADMIN";

    /** Dashboard 趋势图天数：近 14 天（含今天），与前端图表数据范围一致 */
    private static final int TREND_DAYS = 14;

    private final UserMapper userMapper;

    /**
     * {@inheritDoc}
     * @param userId 用户 ID
     * @return 用户信息（不含密码）
     */
    @Override
    public UserVO getUserById(Long userId) {
        // 1.通过用户Id查询数据库
        User user = userMapper.selectById(userId);

        // 2.用户不存在则抛出异常
        if(user == null){
            throw new BusinessException(ResultCode.NOT_FOUND,"用户不存在");
        }

        // 3.把数据库查询结果转换成UserVO并返回
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    /**
     * {@inheritDoc}
     * @param targetUserId  目标用户 ID
     * @param currentUserId 当前登录用户 ID（未登录为 null）
     * @return 用户公开信息（联系方式按登录态裁剪）
     */
    @Override
    public UserPublicVO getPublicUserById(Long targetUserId, Long currentUserId) {
        // 1.通过用户Id查询数据库
        User user = userMapper.selectById(targetUserId);

        // 2.用户不存在则抛出异常（隐藏具体原因，统一提示）
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        // 3.只装配"他人可见"的字段：昵称/头像/简介/注册时间，
        //   刻意不 setUsername / setRole / setStatus，避免过度暴露
        UserPublicVO vo = new UserPublicVO();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setBio(user.getBio());
        vo.setCreatedAt(user.getCreatedAt());

        // 4.联系方式只做登录门控：注册时手机号必填，登录用户之间互相可见是既定口径；
        //   未登录时不下发号码，只下发"为什么看不到"
        boolean loggedIn = currentUserId != null;
        vo.setPhoneVisible(loggedIn);
        vo.setPhone(loggedIn && user.getPhone() != null ? user.getPhone() : "");
        vo.setContactHint(loggedIn ? "" : "登录后即可查看联系方式");
        return vo;
    }

    /**
     * {@inheritDoc}
     * @param userDTO 用户资料
     * @return
     */
    @Override
    public UserVO updateUser(UserDTO userDTO) {
        // 1.通过用户Id查询数据库
        User user = userMapper.selectById(UserContext.getUserId());

        // 2.用户不存在则抛出异常
        if(user == null){
            throw new BusinessException(ResultCode.NOT_FOUND,"用户不存在");
        }

        // 3.更新用户信息
        BeanUtils.copyProperties(userDTO, user);
        userMapper.updateById(user);

        // 4.将更新后的用户信并封装成VO返回
        UserVO userVo = new UserVO();
        BeanUtils.copyProperties(user, userVo);
        return userVo;
    }

    @Override
    public List<UserBriefVO> listBriefByIds(List<Long> ids) {
        // 1.空列表保护：避免拼出 "WHERE id IN ()" 这种非法 SQL，也省一次无谓查询
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        // 2.一次批量查询（等价 SQL: WHERE id IN (...)），避免逐个查的 N+1 问题
        List<User> users = userMapper.selectByIds(ids);

        // 3.只挑"名片"字段转成 UserBriefVO（不暴露 username/phone/role 等）
        return users.stream()
                .map(user -> new UserBriefVO(user.getId(), user.getNickname(), user.getAvatar()))
                .toList();
    }

    /**
     * {@inheritDoc}
     * @param query 分页 + 关键词参数
     * @return 用户分页结果（不含密码等敏感字段）
     */
    @Override
    public PageResult<UserVO> pageUsersForAdmin(AdminUserQueryDTO query) {
        // 关键词命中"用户名 OR 昵称 OR 手机号"，必须用 and(...) 把这组 OR 包起来，
        // 否则 OR 优先级低于外层其它条件会导致筛选被短路（OR 泄漏）
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(User::getUsername, query.getKeyword())
                        .or().like(User::getNickname, query.getKeyword())
                        .or().like(User::getPhone, query.getKeyword()))
                .orderByDesc(User::getCreatedAt);

        Page<User> page = userMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        List<UserVO> vos = page.getRecords().stream()
                .map(user -> {
                    UserVO vo = new UserVO();
                    // UserVO 无 passwordHash 字段，copyProperties 天然不会带出密码
                    BeanUtils.copyProperties(user, vo);
                    return vo;
                })
                .toList();
        return PageResult.of(vos, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    /**
     * {@inheritDoc}
     * @param id  目标用户ID
     * @param dto 状态更新参数
     * @return 更新后的用户信息
     */
    @Override
    public UserVO updateUserStatusByAdmin(Long id, UserStatusUpdateDTO dto) {
        String status = dto.getStatus();
        // 1.状态白名单校验：只认 ACTIVE / DISABLED，挡住乱传的值
        if (!STATUS_ACTIVE.equals(status) && !STATUS_DISABLED.equals(status)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "非法的状态值");
        }

        // 2.查用户：不存在 → 404
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        // 3.保护管理员账号：不能被禁用，避免误操作把管理员锁死在系统外
        if (ROLE_ADMIN.equals(user.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "不能操作管理员账号");
        }

        // 4.定向更新：只改 status 一列，updated_at 交给数据库自动维护，不覆盖其它字段
        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, id)
                .set(User::getStatus, status));

        // 5.内存实体同步新状态后转 VO 返回（UserVO 无 updatedAt，无需回查）
        user.setStatus(status);
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    /**
     * {@inheritDoc}
     * @param id 目标用户ID
     */
    @Override
    public void deleteUserByAdmin(Long id) {
        // 1.查用户：不存在 → 404
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        // 2.保护管理员账号：不能被删除
        if (ROLE_ADMIN.equals(user.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "不能删除管理员账号");
        }

        // 3.执行删除
        userMapper.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserStatsVO getUserStatsForAdmin() {
        // 1.趋势窗口：近 14 天（含今天），与前端 Dashboard 图表的数据范围一致
        LocalDate today = LocalDate.now();
        LocalDateTime since = today.minusDays(TREND_DAYS - 1).atStartOfDay();

        // 2.用户总数 + 今日新增（两条 COUNT，量级小，直接 count 而不是拉列表）
        long userTotal = userMapper.selectCount(null);
        long todayNewUsers = userMapper.selectCount(
                new LambdaQueryWrapper<User>().ge(User::getCreatedAt, today.atStartOfDay()));

        // 3.按天聚合注册数，再把"没有注册的日期"补 0，保证 14 天的轴连续：
        //   GROUP BY 只能返回有数据的日期，直接透传会让 ECharts 的 x 轴断档
        Map<String, Long> countByDate = userMapper.countDailyRegisteredSince(since).stream()
                .collect(Collectors.toMap(DailyCountDTO::getStatDate, DailyCountDTO::getCnt));
        List<TrendPointVO> userGrowth = new ArrayList<>(TREND_DAYS);
        for (int i = 0; i < TREND_DAYS; i++) {
            String label = today.minusDays(TREND_DAYS - 1 - i)
                    .format(DateTimeFormatter.ofPattern("MM-dd"));
            userGrowth.add(new TrendPointVO(label, countByDate.getOrDefault(label, 0L)));
        }

        UserStatsVO vo = new UserStatsVO();
        vo.setUserTotal(userTotal);
        vo.setTodayNewUsers(todayNewUsers);
        vo.setUserGrowth(userGrowth);
        return vo;
    }
}