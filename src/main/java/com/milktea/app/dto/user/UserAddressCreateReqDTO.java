// File: milktea-backend/src/main/java/com.milktea.app/dto/user/UserAddressCreateReqDTO.java
package com.milktea.app.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAddressCreateReqDTO {
    @NotBlank(message = "收货人姓名不能为空")
    private String name;
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$", message = "手机号格式不正确")
    private String phone;
    @NotBlank(message = "省份不能为空")
    private String province;
    @NotBlank(message = "城市不能为空")
    private String city;
    private String district;
    @NotBlank(message = "详细地址不能为空")
    private String detail;
    private String postalCode;
    @NotNull(message = "是否默认地址不能为空")
    private Boolean isDefault;
    private String type; // home, company, school, other
    private String label;
    private BigDecimal longitude;
    private BigDecimal latitude;
}