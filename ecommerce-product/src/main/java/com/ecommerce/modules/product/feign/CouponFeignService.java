package com.ecommerce.modules.product.feign;

import com.ecommerce.common.to.SkuReductionTo;
import com.ecommerce.common.to.SpuBoundTo;
import com.ecommerce.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 使用 feign 来在 ecommerce-product 中远程调用 ecommerce-coupon 中的方法
 * SpuInfoServiceImpl 中会用到一些 ecommerce-coupon 中的方法
 */
@FeignClient("ecommerce-coupon")
public interface CouponFeignService {

    /**
     * feign 远程调用其他服务的过程
     * 1. CouponFeignService.saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);
     *      1) @RequestBody 将 spuBoundTo 这个对象转换成 json
     *      2) 找到 ecommerce-coupon 服务，给 /coupon/spubounds/save 发送请求
     *         将上一步中 转换成的 json放在请求体位置，发送请求
     *      3) 对方服务收到请求，收到的是 请求体里的 json数据
     *
     *         public R save(@RequestBody SpuBoundsEntity spuBounds) 方法
     *         将请求体的 json 转化为  SpuBoundsEntity 实例
     *
     *         所以 这里发送的是 SpuBoundTo， 另外一边能否转换成 SpuBoundsEntity？
     *         可以，只要 SpuBoundTo 内的数据，能对应 SpuBoundsEntity 的json就行
     *
     * 只要 json 数据模型是兼容的，双方服务无需使用同一个类
     * @param spuBoundTo
     * @return
     */
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    @PostMapping("/coupon/skufullreduction/saveinfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
