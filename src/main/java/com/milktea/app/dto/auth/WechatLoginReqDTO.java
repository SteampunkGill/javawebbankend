// File: milktea-backend/src/main/java/com.milktea.app/dto/auth/WechatLoginReqDTO.java
package com.milktea.app.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WechatLoginReqDTO {
    @NotBlank(message = "微信登录码不能为空")
    private String code;
    private UserInfo userInfo; // Optional, if mini-program provides it
    private String encryptedData; // Optional for data decryption
    private String iv; // Optional for data decryption

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private String nickName;
        private String avatarUrl;
        private Integer gender; // 0:未知, 1:男, 2:女
        private String country;
        private String province;
        private String city;
    }
}