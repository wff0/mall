package com.wff.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wff.common.to.mq.OrderTo;
import com.wff.common.to.mq.StockLockedTo;
import com.wff.common.utils.PageUtils;
import com.wff.mall.ware.entity.WareSkuEntity;
import com.wff.mall.ware.vo.SkuHasStockVo;
import com.wff.mall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author wangfengfan
 * @email 1098137961@qq.com
 * @date 2021-05-03 12:16:16
 */
public interface WareSkuService extends IService<WareSkuEntity> {


    PageUtils queryPage(Map<String, Object> params);

    /**
     * 保存库存的时候顺便查到商品价格
     */
    double addStock(Long skuId, Long wareId, Integer skuNum);

    /**
     * 查询是否有库存
     */
    List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds);

    /**
     * 为某个订单锁定库存
     */
    boolean orderLockStock(WareSkuLockVo vo);

    void unlockStock(StockLockedTo to);

    /**
     * 由于订单超时而自动释放订单之后来解锁库存
     */
    void unlockStock(OrderTo to);
}

