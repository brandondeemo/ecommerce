package com.ecommerce.ecommerceproduct;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ecommerce.modules.product.EcommerceProductApplication;
import com.ecommerce.modules.product.entity.BrandEntity;
import com.ecommerce.modules.product.service.BrandService;
import com.ecommerce.modules.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.web.reactive.server.JsonPathAssertions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
// unit test

@Slf4j
@SpringBootTest(classes = EcommerceProductApplication.class)
class EcommerceProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Test
    void testRedisson() {
        System.out.println(redissonClient);
    }

    @Test
    void testRedis(){
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        // 保存
        ops.set("hello2", "redis" + UUID.randomUUID().toString());
        // 查询
        String hello = ops.get("hello");
        System.out.println("保存的数据是: " + hello);
    }

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
