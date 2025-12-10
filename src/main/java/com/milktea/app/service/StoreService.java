// File: milktea-backend/src/main/java/com.milktea.app/service/StoreService.java
package com.milktea.app.service;

import com.milktea.app.dto.store.StoreDetailResDTO;
import com.milktea.app.dto.store.StoreNearbyReqDTO;
import com.milktea.app.dto.store.StoreNearbyResDTO;

public interface StoreService {
    StoreNearbyResDTO getNearbyStores(Long userId, StoreNearbyReqDTO reqDTO);
    StoreDetailResDTO getStoreDetail(Long userId, Long storeId);
    void addFavoriteStore(Long userId, Long storeId);
    void removeFavoriteStore(Long userId, Long storeId);
    Boolean isStoreFavorite(Long userId, Long storeId);
}