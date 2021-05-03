package com.wff.mall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wff.common.utils.PageUtils;
import com.wff.mall.coupon.entity.CouponSpuRelationEntity;

import java.util.Map;

/**
 * 优惠券与产品关联
 *
 * @author wangfengfan
 * @email 1098137961@qq.com
 * @date 2021-05-03 11:25:43
 */
public interface CouponSpuRelationService extends IService<CouponSpuRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

