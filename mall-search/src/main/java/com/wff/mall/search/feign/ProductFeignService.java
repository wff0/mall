package com.wff.mall.search.feign;

import com.wff.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/5/20 21:41
 */
@FeignClient("mall-product")
public interface ProductFeignService {

    @GetMapping("/product/attr/info/{attrId}")
    R getAttrsInfo(@PathVariable("attrId") Long attrId);

    @GetMapping("/product/brand/infos")
    R brandInfo(@RequestParam("brandIds") List<Long> brandIds);
}