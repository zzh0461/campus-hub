package com.campushub.campusactivityservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campushub.campusactivityservice.domain.dto.DailyCountDTO;
import com.campushub.campusactivityservice.domain.entity.ActivityRegistration;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动报名 Mapper
 *
 * @author CampusHub
 */
public interface ActivityRegistrationMapper extends BaseMapper<ActivityRegistration> {

    /**
     * 按天统计指定时间之后的报名数（供后台 Dashboard 的报名趋势）
     *
     * 自定义 SQL 的原因：MyBatis-Plus 的 LambdaWrapper 表达不了
     * DATE_FORMAT + GROUP BY 这类数据库函数聚合，只能落到注解 SQL。
     * 只返回"有报名的日期"，缺失日期由 service 补 0，保证趋势轴连续。
     *
     * @param since 起始时间（含当天 00:00:00）
     * @return 按天分组的报名计数列表
     */
    @Select("SELECT DATE_FORMAT(created_at, '%m-%d') AS statDate, COUNT(*) AS cnt "
            + "FROM campus_activity_registration "
            + "WHERE created_at >= #{since} "
            + "GROUP BY DATE_FORMAT(created_at, '%m-%d')")
    List<DailyCountDTO> countDailyRegisteredSince(@Param("since") LocalDateTime since);
}
