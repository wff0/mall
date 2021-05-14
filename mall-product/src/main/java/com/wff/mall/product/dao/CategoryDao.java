package com.wff.mall.product.dao;

import com.wff.mall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author wangfengfan
 * @email 1098137961@qq.com
 * @date 2021-05-07 20:23:14
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
