package com.wff.common.exception;

/**
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/6/1 20:00
 */
public class NotStockException extends RuntimeException{

    private Long skuId;

    public NotStockException(String msg) {
        super(msg + "号商品没有足够的库存了");
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}
