package com.wff.mall.order.vo;

import lombok.Data;

/**
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/6/1 17:13
 */
@Data
public class SkuStockVo {
    private Long skuId;

    private Boolean hasStock;
}
