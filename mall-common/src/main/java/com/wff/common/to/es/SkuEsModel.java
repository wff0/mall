package com.wff.common.to.es;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/5/15 19:44
 */
@Data
public class SkuEsModel {
    private Long skuId;

    private Long spuId;

    private String skuTitle;

    private BigDecimal skuPrice;

    private String skuImg;

    private Long saleCount;

    private Boolean hasStock;

    private Long hotScore;

    private Long brandId;

    private Long catalogId;

    private String brandName;

    private String brandImg;

    private String catalogName;

    private List<Attrs> attrs;

    /**
     * 检索属性
     */
    @Data
    public static class Attrs implements Serializable {
        private Long attrId;

        private String attrName;

        private String attrValue;
    }
}
