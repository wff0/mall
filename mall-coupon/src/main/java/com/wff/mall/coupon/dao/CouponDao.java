package com.wff.mall.coupon.dao;

import com.wff.mall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author wangfengfan
 * @email 1098137961@qq.com
 * @date 2021-05-03 11:25:43
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
