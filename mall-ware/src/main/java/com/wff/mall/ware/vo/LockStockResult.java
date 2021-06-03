package com.wff.mall.ware.vo;

import lombok.Data;

/**
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/6/1 21:41
 */
@Data
public class LockStockResult {
    private Long skuId;

    private Integer num;

    private Boolean locked;
}
