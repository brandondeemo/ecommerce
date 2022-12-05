package com.ecommerce.modules.ware.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.ecommerce.modules.ware.vo.MergeVo;
import com.ecommerce.modules.ware.vo.PurchaseDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.modules.ware.entity.PurchaseEntity;
import com.ecommerce.modules.ware.service.PurchaseService;
import com.ecommerce.common.utils.PageUtils;
import com.ecommerce.common.utils.R;



/**
 * 采购信息
 *
 * @author Wonpyeong Son
 * @email sonwonpyeong@yonsei.ac.kr
 * @date 2021-12-17 02:42:03
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    // /ware/purchase/unreceive/list
    /**
     * 查询未领取的采购单
     */
    @RequestMapping("/unreceive/list")
    public R unreceivedList(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPageUnreceived(params);

        return R.ok().put("page", page);
    }

    /**
     * 合并采购需求
     */
    // /ware/purchase/merge
    @PostMapping("/merge")
    public R merge(@RequestBody MergeVo mergeVo) {

        purchaseService.mergePurchase(mergeVo);

        return R.ok();
    }

    /**
     * 领取采购单
     */
    // /ware/purchase/received
    @PostMapping("/received")
    public R receive(@RequestBody List<Long> Ids) {

        purchaseService.received(Ids);

        return R.ok();
    }

    /**
     * 完成采购单
     */
    // /ware/purchase/done
    @PostMapping("/done")
    public R finish(@RequestBody PurchaseDoneVo purchaseDoneVo) {

        purchaseService.done(purchaseDoneVo);

        return R.ok();
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody PurchaseEntity purchase){
        purchase.setCreateTime(new Date());
        purchase.setUpdateTime(new Date());

		purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody PurchaseEntity purchase){
		purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
