package com.ssafy.newstagram.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableFeignClients
@EntityScan(basePackages = {"com.ssafy.newstagram.domain", "com.ssafy.newstagram.api"})
@EnableJpaRepositories(basePackages = {"com.ssafy.newstagram.domain", "com.ssafy.newstagram.api"})
public class ApiServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiServerApplication.class, args);
    }

}
