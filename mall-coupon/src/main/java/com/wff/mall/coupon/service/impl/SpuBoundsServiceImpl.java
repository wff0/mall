package com.wff.mall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wff.common.to.SpuBoundTo;
import com.wff.common.utils.PageUtils;
import com.wff.common.utils.Query;
import com.wff.mall.coupon.dao.SpuBoundsDao;
import com.wff.mall.coupon.entity.SpuBoundsEntity;
import com.wff.mall.coupon.service.SpuBoundsService;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("spuBoundsService")
public class SpuBoundsServiceImpl extends ServiceImpl<SpuBoundsDao, SpuBoundsEntity> implements SpuBoundsService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuBoundsEntity> page = this.page(
                new Query<SpuBoundsEntity>().getPage(params),
                new QueryWrapper<SpuBoundsEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * gulimall-product远程调用保存满减信息
     *
     * @param spuBoundTo
     */
    @Override
    public void saveSpuBounds(SpuBoundTo spuBoundTo) {
        SpuBoundsEntity spuBoundsEntity = new SpuBoundsEntity();
        spuBoundsEntity.setBuyBounds(spuBoundTo.getBuyBounds());
        spuBoundsEntity.setGrowBounds(spuBoundTo.getGrowBounds());
        spuBoundsEntity.setWork(1);
        spuBoundsEntity.setSpuId(spuBoundTo.getSpuId());
        baseMapper.insert(spuBoundsEntity);
    }

}