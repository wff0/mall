package com.wff.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wff.common.utils.PageUtils;
import com.wff.mall.ware.entity.WareOrderTaskDetailEntity;

import java.util.Map;

/**
 * 库存工作单
 *
 * @author wangfengfan
 * @email 1098137961@qq.com
 * @date 2021-05-03 12:16:16
 */
public interface WareOrderTaskDetailService extends IService<WareOrderTaskDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

