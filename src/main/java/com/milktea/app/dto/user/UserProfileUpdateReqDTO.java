// File: milktea-backend/src/main/java/com.milktea.app/dto/user/UserProfileUpdateReqDTO.java
package com.milktea.app.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateReqDTO {
    @NotBlank(message = "昵称不能为空")
    private String nickname;
    private String avatar;
    private Integer gender;
    private LocalDate birthday;
    @Email(message = "邮箱格式不正确")
    private String email;
}