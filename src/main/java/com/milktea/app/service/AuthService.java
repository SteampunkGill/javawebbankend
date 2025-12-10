// File: milktea-backend/src/main/java/com.milktea.app/service/AuthService.java
package com.milktea.app.service;

import com.milktea.app.dto.auth.AccountLoginReqDTO;
import com.milktea.app.dto.auth.UserAuthResDTO;
import com.milktea.app.dto.auth.WechatLoginReqDTO;

public interface AuthService {
    UserAuthResDTO wechatLogin(WechatLoginReqDTO reqDTO); // 这是微信实现的
    UserAuthResDTO accountLogin(AccountLoginReqDTO reqDTO);
    // Add methods for registration, password reset, logout etc.
    void logout(String token);
}