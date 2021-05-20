package com.wff.mall.product.feign;

import com.wff.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/5/15 20:05
 */
@FeignClient("mall-ware")
public interface WareFeignService {

    /**
     * 修改真个系统的 R 带上泛型
     */
    @PostMapping("/ware/waresku/hasStock")
    R getSkuHasStock(@RequestBody List<Long> SkuIds);
}
