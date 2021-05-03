package com.wff.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wff.common.utils.PageUtils;
import com.wff.mall.product.entity.SpuInfoEntity;

import java.util.Map;

/**
 * spu??Ϣ
 *
 * @author wangfengfan
 * @email 1098137961@qq.com
 * @date 2021-05-03 10:16:29
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

