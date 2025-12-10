// File: milktea-backend/src/main/java/com.milktea.app/dto/auth/UserAuthResDTO.java
package com.milktea.app.dto.auth;

import com.milktea.app.dto.user.UserProfileResDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthResDTO {
    private String token;
    private UserProfileResDTO.UserDetailDTO user; // Nested UserProfileResDTO
}