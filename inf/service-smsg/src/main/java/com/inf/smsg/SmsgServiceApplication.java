package com.inf.smsg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients
@ComponentScan(basePackages = {"com.inf"})
public class SmsgServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmsgServiceApplication.class, args);
    }
}
