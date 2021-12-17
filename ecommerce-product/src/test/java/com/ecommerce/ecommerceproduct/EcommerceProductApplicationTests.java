package com.ecommerce.ecommerceproduct;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ecommerce.modules.product.EcommerceProductApplication;
import com.ecommerce.modules.product.entity.BrandEntity;
import com.ecommerce.modules.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
// unit test

@SpringBootTest(classes = EcommerceProductApplication.class)
class EcommerceProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Test
    void contextLoads() {
//        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setBrandId(1L);
//        brandEntity.setDescript("apple is good");

//        brandEntity.setName("apple");
//        brandService.save(brandEntity);
//        System.out.println("set name succuess");

//        brandService.updateById(brandEntity);
        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1L));

        list.forEach((item)->{
            System.out.println(item);
        });

    }

}
