package com.wff.mall.order.feign;

import com.wff.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/6/1 19:56
 */
@FeignClient("mall-product")
public interface ProductFeignService {
    @GetMapping("/product/spuinfo/skuId/{id}")
    R getSkuInfoBySkuId(@PathVariable("id") Long skuId);
}
