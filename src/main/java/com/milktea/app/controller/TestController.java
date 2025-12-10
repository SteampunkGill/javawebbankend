// File: src/main/java/com/milktea/app/controller/TestController.java
package com.milktea.app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello, Milktea Backend is running! Time: " + new java.util.Date();
    }

    @GetMapping("/health")
    public String health() {
        return "Status: OK";
    }

    @GetMapping("/info")
    public String info() {
        return "Milktea Backend API v1.0";
    }
}