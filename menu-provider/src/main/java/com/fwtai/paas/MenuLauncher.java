package com.fwtai.paas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MenuLauncher{

    public static void main(final String[] args) {
        SpringApplication.run(MenuLauncher.class, args);
    }
}