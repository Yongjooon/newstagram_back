package com.ssafy.newstagram.logging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"com.ssafy.newstagram.domain", "com.ssafy.newstagram.logging"})
@EnableJpaRepositories(basePackages = {"com.ssafy.newstagram.domain", "com.ssafy.newstagram.logging"})
public class LoggingServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoggingServerApplication.class, args);
    }

}
