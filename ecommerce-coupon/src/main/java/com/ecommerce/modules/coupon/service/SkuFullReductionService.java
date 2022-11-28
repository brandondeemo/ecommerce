package com.ecommerce.modules.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.common.to.SkuReductionTo;
import com.ecommerce.common.utils.PageUtils;
import com.ecommerce.modules.coupon.entity.SkuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author Wonpyeong Son
 * @email sonwonpyeong@yonsei.ac.kr
 * @date 2021-12-17 01:44:08
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuReduction(SkuReductionTo skuReductionTo);
}

