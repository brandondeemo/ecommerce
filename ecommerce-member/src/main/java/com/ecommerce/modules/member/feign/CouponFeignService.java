package com.ecommerce.modules.member.feign;

import com.ecommerce.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * 这是一个声明式的远程调用
 */
@FeignClient("ecommerce-coupon")
public interface CouponFeignService {

    // 注意 第一个 / 必不可少
    /*
    下面的意思是：
    如果调用 CouponFeignService接口 中的 membercoupons方法
    程序会先去注册中心中 找到"ecommerce-coupon" 所在的位置
    再去调用 "/coupon/coupon/member/list" 请求对应的方法
     */
    @RequestMapping("/coupon/coupon/member/list")
    public R membercoupons();
}
