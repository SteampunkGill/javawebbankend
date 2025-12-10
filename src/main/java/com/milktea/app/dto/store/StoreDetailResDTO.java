package com.milktea.app.dto.store;

import com.milktea.app.dto.home.HomePageResDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StoreDetailResDTO extends HomePageResDTO.NearbyStoreDTO {
    // 你的字段...
}