package com.wff.mall.product.feign;

import com.wff.common.to.es.SkuEsModel;
import com.wff.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/5/15 20:03
 */
@FeignClient("mall-search")
public interface SearchFeignService {

    @PostMapping("/search/save/product")
    R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
