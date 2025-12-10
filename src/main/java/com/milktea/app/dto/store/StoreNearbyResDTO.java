// File: milktea-backend/src/main/java/com.milktea.app/dto/store/StoreNearbyResDTO.java
package com.milktea.app.dto.store;

import com.milktea.app.dto.home.HomePageResDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreNearbyResDTO {
    private List<HomePageResDTO.NearbyStoreDTO> stores;
    private CurrentLocationDTO currentLocation;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentLocationDTO {
        private BigDecimal longitude;
        private BigDecimal latitude;
        private String address;
    }
}