package com.wff.mall.coupon.controller;

import java.util.Arrays;
import java.util.Map;

import com.wff.common.to.SpuBoundTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.wff.mall.coupon.entity.SpuBoundsEntity;
import com.wff.mall.coupon.service.SpuBoundsService;
import com.wff.common.utils.PageUtils;
import com.wff.common.utils.R;



/**
 * 商品spu积分设置
 *
 * @author wangfengfan
 * @email 1098137961@qq.com
 * @date 2021-05-03 11:25:44
 */
@RestController
@RequestMapping("coupon/spubounds")
public class SpuBoundsController {
    @Autowired
    private SpuBoundsService spuBoundsService;

    /**
     * 可以使用save的本方法，因为@requestbody 无需使接受数据类型相同
     *
     * @return
     */
    @PostMapping("/saveSpuBoundTo")
    public R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo) {
        spuBoundsService.saveSpuBounds(spuBoundTo);
        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("coupon:spubounds:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = spuBoundsService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("coupon:spubounds:info")
    public R info(@PathVariable("id") Long id){
		SpuBoundsEntity spuBounds = spuBoundsService.getById(id);

        return R.ok().put("spuBounds", spuBounds);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody SpuBoundsEntity spuBounds) {
        spuBoundsService.save(spuBounds);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("coupon:spubounds:update")
    public R update(@RequestBody SpuBoundsEntity spuBounds){
		spuBoundsService.updateById(spuBounds);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("coupon:spubounds:delete")
    public R delete(@RequestBody Long[] ids){
		spuBoundsService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
