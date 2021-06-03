package com.wff.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/6/1 17:10
 */
@Data
public class FareVo {
    private MemberAddressVo memberAddressVo;

    private BigDecimal fare;
}
