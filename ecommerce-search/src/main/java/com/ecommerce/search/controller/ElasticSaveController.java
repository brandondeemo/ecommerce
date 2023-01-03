package com.ecommerce.search.controller;

import com.ecommerce.common.exception.BizCodeEnum;
import com.ecommerce.common.to.es.SkuEsModel;
import com.ecommerce.common.utils.R;
import com.ecommerce.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/search/save")
public class ElasticSaveController {
    @Autowired
    ProductSaveService productSaveService;

    // 上架商品
    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels) {
        boolean b = false;

        try {
            b = productSaveService.productStatusUp(skuEsModels);
        } catch (Exception e) {
            log.error("ElasticSearchController 商品上架错误: {}", e);
            return R.error(BizCodeEnum.PRODUCT_DP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_DP_EXCEPTION.getMessage());
        }

        if (!b) {
            return R.ok();
        } else {
            return R.error(BizCodeEnum.PRODUCT_DP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_DP_EXCEPTION.getMessage());
        }

    }
}
