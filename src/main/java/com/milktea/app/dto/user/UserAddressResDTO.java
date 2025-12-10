// File: milktea-backend/src/main/java/com.milktea.app/dto/user/UserAddressResDTO.java
package com.milktea.app.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAddressResDTO {
    private List<AddressDTO> list;
    private Integer total;
    private Integer page;
    private Integer limit;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressDTO {
        private Long id;
        private String name;
        private String phone;
        private String province;
        private String city;
        private String district;
        private String detail;
        private String postalCode;
        private Boolean isDefault;
        private String type;
        private String label;
        private BigDecimal longitude;
        private BigDecimal latitude;
        private Instant createdAt;
    }
}