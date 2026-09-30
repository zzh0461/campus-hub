package com.campushub.campusauthservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campushub.campusauthservice.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper 接口
 *
 * 提供用户表的基本 CRUD 操作。
 * 继承 BaseMapper 获得 MyBatis-Plus 的通用方法。
 *
 * @author CampusHub
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    // MyBatis-Plus 已提供基础的 CRUD 方法
    // 如需自定义查询，可在此添加方法
}
