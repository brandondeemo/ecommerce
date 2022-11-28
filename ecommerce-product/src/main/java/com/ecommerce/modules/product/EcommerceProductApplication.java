package com.ecommerce.modules.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
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
