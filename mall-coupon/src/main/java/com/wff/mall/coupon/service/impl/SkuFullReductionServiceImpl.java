package com.wff.mall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wff.common.to.MemberPrice;
import com.wff.common.to.SkuReductionTo;
import com.wff.common.utils.PageUtils;
import com.wff.common.utils.Query;
import com.wff.mall.coupon.dao.SkuFullReductionDao;
import com.wff.mall.coupon.entity.MemberPriceEntity;
import com.wff.mall.coupon.entity.SkuFullReductionEntity;
import com.wff.mall.coupon.entity.SkuLadderEntity;
import com.wff.mall.coupon.service.MemberPriceService;
import com.wff.mall.coupon.service.SkuFullReductionService;
import com.wff.mall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    SkuLadderService skuLadderService;

    @Autowired
    MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {

        // 保存sku的优惠、满减、会员价格等信息  [跨库] sms_sku_ladder
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuReductionTo.getSkuId());
        skuLadderEntity.setFullCount(skuReductionTo.getFullCount());
        skuLadderEntity.setDiscount(skuReductionTo.getDiscount());
        // 是否参加其他优惠
        skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
        // 有的满减条件才保存
        if (skuReductionTo.getFullCount() > 0) {
            skuLadderService.save(skuLadderEntity);
        }
        skuLadderService.save(skuLadderEntity);

        // sms_sku_full_reduction
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTo, skuFullReductionEntity);
        if ((skuFullReductionEntity.getFullPrice().compareTo(new BigDecimal("0")) > 0)) {
            this.save(skuFullReductionEntity);
        }

        // sms_member_price  保存价格等属性
        List<MemberPrice> memberPrice = skuReductionTo.getMemberPrice();
        List<MemberPriceEntity> collect = memberPrice.stream().map(m -> {
            MemberPriceEntity priceEntity = new MemberPriceEntity();
            priceEntity.setSkuId(skuReductionTo.getSkuId());
            priceEntity.setMemberLevelId(m.getId());
            priceEntity.setMemberLevelName(m.getName());
            priceEntity.setMemberPrice(m.getPrice());
            priceEntity.setAddOther(1);

            return priceEntity;

        }).filter(item ->
                // 输入的商品价格必须要大于0才保存
                (item.getMemberPrice().compareTo(new BigDecimal("0")) > 0)
        ).collect(Collectors.toList());
        memberPriceService.saveBatch(collect);
    }

}