package com.wff.mall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * <p>Title: SearchParam</p>
 * Description：封装页面所有可能传递过来的关键字
 * 		catalog3Id=225&keyword=华为&sort=saleCount_asc&hasStock=0/1&brandId=25&brandId=30
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/5/19 20:25
 */
@Data
public class SearchParam {

    /**
     * 全文匹配关键字
     */
    private String keyword;

    /**
     * 三级分类id
     */
    private Long catalog3Id;

    private String sort;

    private Integer hasStock;

    /**
     * 价格区间
     */
    private String skuPrice;

    /**
     * 品牌id 可以多选
     */
    private List<Long> brandId;

    /**
     * 按照属性进行筛选
     */
    private List<String> attrs;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 原生所有查询属性
     */
    private String _queryString;
}
