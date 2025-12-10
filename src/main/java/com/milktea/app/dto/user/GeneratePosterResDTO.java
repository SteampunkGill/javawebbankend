// File: milktea-backend/src/main/java/com.milktea.app/dto/user/GeneratePosterResDTO.java
package com.milktea.app.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneratePosterResDTO {
    private String posterUrl;
    private Instant expireAt;
}