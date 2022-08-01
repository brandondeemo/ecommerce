package com.ecommerce.modules.coupon;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.ArrayList;

/**
 * （一）如何使用 nacos 作为配置中心统一管理配置
 *
 * 1. 引入依赖
 * 2. 创建一个 bootstrap.properties (该名称是固定的)
 *    spring.application.name=ecommerce-coupon
 *    spring.cloud.nacos.config.server-addr=127.0.0.1:8848=127.0.0.1:8848
 * 3. 需要给配置中心默认添加一个叫 数据集（Data ID,
 * 默认名：application name + .properties
 * 4. 给应用名.properties 添加任何配置
 * 5. 动态获取配置
 *    添加 @RefreshScope  // 动态获取并刷新配置
 *        @Value("${配置项的名}$")，获取到配置
 *    如果配置中心和当前应用的配置(application.properties)文件中 有相同的项
 *    那么优先使用配置中心的项
 *
 * （二）细节
 * 1. 命名空间：配置隔离
 *      默认：public 默认新增的所有配置都在 public 空间
 *      1. 开发，配置，生产 都是不同的环境，可利用命名空间来做环境隔离
 *      注意：在 bootstrap.properties 配置哪个命名空间下的配置
 *      spring.cloud.nacos.config.namespace= uuid
 *      2. 每一个微服务之间互相隔离配置，每一个微服务都创建自己的命名空间，
 *      这样只加载自己空间下的配置
 * 2. 配置集：所有配置的集合
 * 3. 配置集ID：类似配置文件名
 *    data ID
 *
 * 4. 配置分组：
 *    默认所有配置集都属于 default_group
 *
 *
 *  整体使用思想：
 *  每个微服务都创建专属的命名空间，使用配置分组来区分环境
 */

@EnableDiscoveryClient
@SpringBootApplication
public class EcommerceCouponApplication {
    public static void main(String[] args) {
        SpringApplication.run(EcommerceCouponApplication.class, args);
    }
}
