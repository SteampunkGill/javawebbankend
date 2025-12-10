// File: src/main/java/com/milktea/app/controller/WelcomeController.java
package com.milktea.app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WelcomeController {

    @GetMapping("/")
    public String welcome() {
        return "欢迎使用温馨奶茶小程序后端API！<br>" +
                "API版本: v1.0<br>" +
                "状态: 运行中<br>" +
                "时间: " + new java.util.Date() + "<br>" +
                "测试端点: <a href='/v1/test/hello'>/v1/test/hello</a><br>" +
                "健康检查: <a href='/v1/test/health'>/v1/test/health</a>";
    }
}