package com.ecommerce.ecommercethirdparty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class EcommerceThirdPartyApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceThirdPartyApplication.class, args);
    }

}
