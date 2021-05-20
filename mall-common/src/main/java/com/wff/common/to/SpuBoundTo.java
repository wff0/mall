package com.wff.common.to;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SpuBoundTo {
    private Long spuId;
    /**
     * 购买积分
     */
    private BigDecimal buyBounds;
    /**
     * 成长积分
     */
    private BigDecimal growBounds;
}
