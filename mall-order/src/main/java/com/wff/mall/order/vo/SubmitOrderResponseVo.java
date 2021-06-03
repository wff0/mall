package com.wff.mall.order.vo;

import com.wff.mall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/6/1 17:13
 */
@Data
public class SubmitOrderResponseVo {
    private OrderEntity orderEntity;

    /**
     * 错误状态码： 0----成功
     */
    private Integer code;
}
