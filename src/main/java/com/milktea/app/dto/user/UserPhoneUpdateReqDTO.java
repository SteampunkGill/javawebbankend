// File: milktea-backend/src/main/java/com.milktea.app/dto/user/UserPhoneUpdateReqDTO.java
package com.milktea.app.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPhoneUpdateReqDTO {
    @NotBlank(message = "新手机号不能为空")
    @Pattern(regexp = "^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$", message = "手机号格式不正确")
    private String phone;
    @NotBlank(message = "短信验证码不能为空")
    private String captcha;
}