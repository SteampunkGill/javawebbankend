// File: milktea-backend/src/main/java/com.milktea.app/dto/member/MemberInfoResDTO.java
package com.milktea.app.dto.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberInfoResDTO {
    private String level;
    private String levelName;
    private Integer growthValue;
    private String nextLevel;
    private String nextLevelName;
    private Integer needGrowthValue;
    private LocalDate expireDate;
    private List<PrivilegeDTO> privileges;
    private BirthdayGiftDTO birthdayGift;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrivilegeDTO {
        private String id;
        private String name;
        private String icon;
        private String description;
        private String status;
        private LocalDate availableDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BirthdayGiftDTO {
        private Boolean available;
        private List<GiftItemDTO> gifts;
        private LocalDate expireDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GiftItemDTO {
        private String type;
        private String name;
        private String value;
    }
}