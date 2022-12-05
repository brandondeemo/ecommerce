package com.ecommerce.modules.ware.feign;

import com.ecommerce.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("ecommerce-product")
public interface ProductFeignService {

    /**
     *   /product/skuinfo/info/{skuId}
     *   /api/product/skuinfo/info/{skuId}
     *
     *   feign 有两种写法：
     *   1. 让所有请求过 gateway:
     *       @FeignClient("ecommerce-gateway"): 给 ecommerce-gateway 发请求
     *       @RequestMapping("/api/product/skuinfo/info/{skuId}")
     *
     *   2. 直接让指定的服务处理
     *       @FeignClient("ecommerce-product")
     *       @RequestMapping("/product/skuinfo/info/{skuId}")
     * @param skuId
     * @return
     */
    @RequestMapping("/product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);

}
