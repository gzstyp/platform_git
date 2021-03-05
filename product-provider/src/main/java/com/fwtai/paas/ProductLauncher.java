package com.fwtai.paas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ProductLauncher{

    public static void main(final String[] args) {
        SpringApplication.run(ProductLauncher.class, args);
    }
}