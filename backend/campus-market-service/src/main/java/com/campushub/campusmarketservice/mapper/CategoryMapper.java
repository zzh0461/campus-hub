package com.campushub.campusmarketservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campushub.campusmarketservice.domain.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分类 Mapper 接口
 *
 * 继承 BaseMapper 获得通用 CRUD 能力，查全部分类无需自己写 SQL。
 *
 * @author CampusHub
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}