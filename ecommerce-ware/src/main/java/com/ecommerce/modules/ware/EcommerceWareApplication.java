package com.ecommerce.modules.ware;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class EcommerceWareApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceWareApplication.class, args);
    }

}
