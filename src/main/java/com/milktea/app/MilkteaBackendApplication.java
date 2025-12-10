package com.milktea.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class MilkteaBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(MilkteaBackendApplication.class, args);
    }
}