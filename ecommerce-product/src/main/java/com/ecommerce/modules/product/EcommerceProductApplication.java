package com.ecommerce.modules.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;

@MapperScan("com.ecommerce.modules.product.dao")
@SpringBootApplication
public class EcommerceProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(EcommerceProductApplication.class, args);
    }
}
