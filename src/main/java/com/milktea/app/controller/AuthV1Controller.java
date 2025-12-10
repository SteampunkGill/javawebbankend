// File: milktea-backend/src/main/java/com.milktea.app/controller/AuthV1Controller.java
package com.milktea.app.controller;

import com.milktea.app.common.ApiResponse;
import com.milktea.app.dto.auth.AccountLoginReqDTO;
import com.milktea.app.dto.auth.UserAuthResDTO;
import com.milktea.app.dto.auth.WechatLoginReqDTO;
import com.milktea.app.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthV1Controller {

    private final AuthService authService;

    @PostMapping("/wechat-login") // Updated path to match /auth/wechat-login
    public ApiResponse<UserAuthResDTO> wechatLogin(@Valid @RequestBody WechatLoginReqDTO reqDTO) {
        // 这是微信实现的
        log.info("WeChat login request received: {}", reqDTO.getCode());
        UserAuthResDTO resDTO = authService.wechatLogin(reqDTO);
        return ApiResponse.success(resDTO);
    }

    @PostMapping("/login") // Updated path to match /auth/login
    public ApiResponse<UserAuthResDTO> accountLogin(@Valid @RequestBody AccountLoginReqDTO reqDTO) {
        log.info("Account login request received for username: {}", reqDTO.getUsername());
        UserAuthResDTO resDTO = authService.accountLogin(reqDTO);
        return ApiResponse.success(resDTO);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);
            log.info("User logged out.");
        }
        return ApiResponse.success();
    }

    // Placeholder for registration, password reset etc.
    // @PostMapping("/register")
    // public ApiResponse<Void> register(@Valid @RequestBody RegisterReqDTO reqDTO) { ... }
}