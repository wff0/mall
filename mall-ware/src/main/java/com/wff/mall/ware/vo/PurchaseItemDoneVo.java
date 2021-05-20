package com.wff.mall.ware.vo;

import lombok.Data;

@Data
public class PurchaseItemDoneVo {
    /**
     * "itemId":1,"status":3,"reason":"",
     * "itemId":3,"status":4,"reason":"无货"
     */
    private Long itemId;

    private Integer status;

    private String reason;
}
