package com.ecommerce.modules.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class EcommerceOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceOrderApplication.class, args);
    }

}
