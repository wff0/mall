package com.wff.mall.order.vo;

import lombok.Data;

import java.util.List;

/**
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/6/1 17:14
 */
@Data
public class WareSkuLockVo {
    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 要锁住的所有库存信息
     */
    private List<OrderItemVo> locks;

}
