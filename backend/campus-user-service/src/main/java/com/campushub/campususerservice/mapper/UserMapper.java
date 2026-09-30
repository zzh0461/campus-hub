package com.campushub.campususerservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campushub.campususerservice.domain.dto.DailyCountDTO;
import com.campushub.campususerservice.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户 Mapper 接口
 *
 * 继承 BaseMapper 获得 MyBatis-Plus 的通用 CRUD 能力，
 * 简单的增删改查无需编写 SQL；如有复杂查询可在此扩展自定义方法。
 *
 * @author CampusHub
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 按天统计指定时间之后的注册人数（供后台 Dashboard 的用户增长趋势）
     *
     * 自定义 SQL 的原因：MyBatis-Plus 的 LambdaWrapper 表达不了
     * DATE_FORMAT + GROUP BY 这类数据库函数聚合，只能落到注解 SQL。
     * 只返回"有注册的日期"，缺失日期由 service 补 0，保证趋势轴连续。
     *
     * @param since 起始时间（含当天 00:00:00）
     * @return 按天分组的注册计数列表
     */
    @Select("SELECT DATE_FORMAT(created_at, '%m-%d') AS statDate, COUNT(*) AS cnt "
            + "FROM campus_user "
            + "WHERE created_at >= #{since} "
            + "GROUP BY DATE_FORMAT(created_at, '%m-%d')")
    List<DailyCountDTO> countDailyRegisteredSince(@Param("since") LocalDateTime since);
}