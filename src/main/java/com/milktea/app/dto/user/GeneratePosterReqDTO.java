// File: milktea-backend/src/main/java/com.milktea.app/dto/user/GeneratePosterReqDTO.java
package com.milktea.app.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneratePosterReqDTO {
    private String templateId;
    @NotBlank(message = "小程序路径不能为空")
    private String qrCodeContent;
}