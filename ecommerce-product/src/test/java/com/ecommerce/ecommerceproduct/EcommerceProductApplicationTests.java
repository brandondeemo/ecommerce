package com.ecommerce.ecommerceproduct;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ecommerce.modules.product.EcommerceProductApplication;
import com.ecommerce.modules.product.entity.BrandEntity;
import com.ecommerce.modules.product.service.BrandService;
import com.ecommerce.modules.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
// unit test

@Slf4j
@SpringBootTest(classes = EcommerceProductApplication.class)
class EcommerceProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Test
    void testFindPath(){
        Long[] categoryPath = categoryService.findCategoryPath(225L);
        log.info("完整路径: {}", Arrays.asList(categoryPath));
    }

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
