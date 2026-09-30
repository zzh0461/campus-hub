package com.campushub.campusmarketservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campushub.campusmarketservice.domain.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品收藏 Mapper 接口
 *
 * 继承 BaseMapper 获得 MyBatis-Plus 的通用 CRUD 能力。
 *
 * @author CampusHub
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}