package com.wff.mall.serach.service;

import com.wff.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/5/15 20:51
 */
public interface ProductSaveService {
    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
