package com.ecommerce.modules.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/*
1. 开启服务注册发现
 （配置nacos的注册中心地址）
 */

@EnableDiscoveryClient
/*
gateway 引用common
common 中有 mybatis，mybatis需要数据源
这里排除数据源
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class EcommerceGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceGatewayApplication.class, args);
    }

}
