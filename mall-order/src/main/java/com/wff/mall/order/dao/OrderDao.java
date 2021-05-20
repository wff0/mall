package com.wff.mall.order.dao;

import com.wff.mall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author wangfengfan
 * @email 1098137961@qq.com
 * @date 2021-05-03 12:11:24
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
