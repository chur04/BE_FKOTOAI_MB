package com.g5.fokotoai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableJpaRepositories("com.g5.fokotoai.repository")
public class FokotoaiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FokotoaiApplication.class, args);
    }

}