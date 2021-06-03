package com.wff.mall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/5/24 21:35
 */
@Data
public class SpuItemAttrGroupVo {
    private String groupName;

    private List<SpuBaseAttrVo> attrs;
}
