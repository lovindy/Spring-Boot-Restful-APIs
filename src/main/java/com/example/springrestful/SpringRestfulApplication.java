package com.example.springrestful;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringRestfulApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringRestfulApplication.class, args);
    }
}
