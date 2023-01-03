package com.ecommerce.modules.product.feign;

import com.ecommerce.common.to.SkuHasStockVo;
import com.ecommerce.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("ecommerce-ware")
public interface WareFeignService {

    /*
    三种方法
    1. R 设计的时候可以加上范型
    2. 直接返回我们想要的结果
    3。 自己封装解析结果
     */
    @PostMapping("/ware/waresku/hasstock")
    R getSkuHasStock(@RequestBody List<Long> skuIds);
}
