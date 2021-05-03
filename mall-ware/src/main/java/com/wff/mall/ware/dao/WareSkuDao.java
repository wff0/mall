package com.wff.mall.ware.dao;

import com.wff.mall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品库存
 * 
 * @author wangfengfan
 * @email 1098137961@qq.com
 * @date 2021-05-03 12:16:16
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {
	
}
