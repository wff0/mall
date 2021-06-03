package com.wff.mall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/5/24 21:41
 */
@Data
public class SkuItemSaleAttrVo {
    private Long attrId;

    private String attrName;

    private List<AttrValueWithSkuIdVo> attrValues;
}
