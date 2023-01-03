package com.ecommerce.modules.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 1. 整合mybatis-plus
 *      1. 导入依赖
 *         <dependency>
 *             <groupId>com.baomidou</groupId>
 *             <artifactId>mybatis-plus-boot-starter</artifactId>
 *             <version>3.3.2</version>
 *         </dependency>
 *      2. 配置
 *          1. 配置数据源：
 *              1.导入数据库驱动
 *              2. application.yml里配置数据源相关信息
 *          2. 配置Mybatis-plus:
 *              1. 使用@MapperScan
 *              2. 告诉Mybatis-plus, sql映射文件位置
 * 2. 逻辑删除
 * 1）配置全局的逻辑删除规则(省略，版本高)
 * 2）配置删除逻辑的组建bean(省略，版本高)
 * 3）给Bean加上逻辑删除注释@TableLogic
 *
 *
 * 5. 模板引擎
 *  1) thymeleaf-starter: 关闭缓存
 *  2) 静态资源都放在 static 文件夹下就可以按照路径直接访问
 *  3) 页面放在templates 下，直接访问
 *  spring boot 访问项目的时候，默认会找 index.html
 *  4) 页面修改 在不重启服务器情况下 实时更新
 *     1. 引入 dev-tools
 *
 * 6. 整合 redis
 *  1) 引入 data-redis-starter
 *  2) 简单配置 redis 的Host信息
 *  3）使用 springboot自动配置好的 StringRedisTemplate 来操作 redis
 *
 * 7. 整合 redisson 作为分布式功能框架
 *  1) 引入依赖
 *  2) 配置
 *
 * 8. 整合 springcache 简化缓存开发
 *  1) 引入依赖
 *  2) 写配置
 *     (1) 自动配置了那些？
 *     cacheAutoConfiguration 会导入 RedisCacheConfiguration
 *     自动配好了缓存管理器
 *     (2) 我们要配哪些？
 *     application.properties 中配置
 *     配置使用 redis 作为缓存
 *     (3) 测试使用缓存
 *        1. 开启缓存功能  @EnableCaching
 *        2. 只需要使用注解就能完成缓存
 *     (4) cacheAutoConfiguration -> RedisCacheConfiguration ->
 *     自动配置了 RedisCacheManager -> 初始化所有的缓存 -> 每个缓存决定使用什么配置
 *     -> 如果 RedisCacheConfiguration 有就用已有的，没有就用默认配置 ->
 *     想改缓存的配置，只需要给容器中放一个 RedisCacheConfiguration 即可
 *     -> 就会应用到当前 RedisCacheManager 管理的所有缓存分区中
 */



@EnableFeignClients("com.ecommerce.modules.product.feign")
@EnableDiscoveryClient
@MapperScan("com.ecommerce.modules.product.dao")
@SpringBootApplication
public class EcommerceProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(EcommerceProductApplication.class, args);
    }
}
