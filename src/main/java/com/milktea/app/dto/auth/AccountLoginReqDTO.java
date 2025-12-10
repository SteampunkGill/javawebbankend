// File: milktea-backend/src/main/java/com/milktea.app/dto/auth/AccountLoginReqDTO.java
package com.milktea.app.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountLoginReqDTO {
    @NotBlank(message = "用户名/手机号/邮箱不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
    private String captcha; // Optional
}