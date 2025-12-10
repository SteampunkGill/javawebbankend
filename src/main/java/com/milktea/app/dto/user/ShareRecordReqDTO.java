// File: milktea-backend/src/main/java/com.milktea.app/dto/user/ShareRecordReqDTO.java
package com.milktea.app.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShareRecordReqDTO {
    @NotBlank(message = "分享类型不能为空")
    private String type; // product, activity, invite
    @NotBlank(message = "目标ID不能为空")
    private String targetId;
    @NotBlank(message = "分享渠道不能为空")
    private String channel; // wechat, moments, qq
}