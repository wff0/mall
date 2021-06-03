package com.wff.mall.ware.feign;

import com.wff.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/6/1 21:47
 */
@FeignClient("mall-order")
public interface OrderFeignService {
    /**
     * 查询订单状态
     */
    @GetMapping("/order/order/status/{orderSn}")
    R getOrderStatus(@PathVariable("orderSn") String orderSn);
}
