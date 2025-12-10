// File: milktea-backend/src/main/java/com.milktea.app/service/impl/StoreServiceImpl.java
package com.milktea.app.service.impl;

import com.milktea.app.common.constant.ErrorCode;
import com.milktea.app.common.exception.BusinessException;
import com.milktea.app.common.util.GeoUtil;
import com.milktea.app.dto.home.HomePageResDTO;
import com.milktea.app.dto.store.StoreDetailResDTO;
import com.milktea.app.dto.store.StoreNearbyReqDTO;
import com.milktea.app.dto.store.StoreNearbyResDTO;
import com.milktea.app.entity.StoreEntity;
import com.milktea.app.entity.StoreImageEntity;
import com.milktea.app.entity.StoreServiceEntity;
import com.milktea.app.entity.StoreTagEntity;
import com.milktea.app.entity.UserEntity;
import com.milktea.app.entity.UserFavoriteStoreEntity;
import com.milktea.app.repository.StoreRepository;
import com.milktea.app.repository.UserFavoriteStoreRepository;
import com.milktea.app.repository.UserRepository;
import com.milktea.app.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.Instant; // 添加这行导入
@Service
@RequiredArgsConstructor
@Slf4j
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final UserFavoriteStoreRepository userFavoriteStoreRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public StoreNearbyResDTO getNearbyStores(Long userId, StoreNearbyReqDTO reqDTO) {
        List<StoreEntity> nearbyStores = storeRepository.findNearbyStoresNative(
                reqDTO.getLatitude(), reqDTO.getLongitude(), reqDTO.getRadius().doubleValue(), reqDTO.getLimit()
        );

        List<HomePageResDTO.NearbyStoreDTO> storeDTOs = nearbyStores.stream()
                .map(entity -> mapToNearbyStoreDTO(entity, reqDTO.getLatitude(), reqDTO.getLongitude(), userId))
                .collect(Collectors.toList());

        StoreNearbyResDTO resDTO = new StoreNearbyResDTO();
        resDTO.setStores(storeDTOs);
        resDTO.setCurrentLocation(new StoreNearbyResDTO.CurrentLocationDTO(
                reqDTO.getLongitude(), reqDTO.getLatitude(), "当前位置 (mock)"
        ));
        return resDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public StoreDetailResDTO getStoreDetail(Long userId, Long storeId) {
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND, "Store not found."));

        // Assume user's current location is needed for distance calculation, mock for now
        BigDecimal mockUserLat = BigDecimal.valueOf(39.916527);
        BigDecimal mockUserLon = BigDecimal.valueOf(116.397128);

        return mapToStoreDetailResDTO(store, mockUserLat, mockUserLon, userId);
    }

    @Override
    @Transactional
    public void addFavoriteStore(Long userId, Long storeId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User not found."));
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND, "Store not found."));

        if (userFavoriteStoreRepository.existsByUserIdAndStoreId(userId, storeId)) {
            log.info("Store {} is already favorited by user {}", storeId, userId);
            return;
        }

        UserFavoriteStoreEntity favorite = new UserFavoriteStoreEntity();
        favorite.setUser(user);
        favorite.setStore(store);
        favorite.setCreatedAt(Instant.now());
        userFavoriteStoreRepository.save(favorite);
        log.info("User {} favorited store {}", userId, storeId);
    }

    @Override
    @Transactional
    public void removeFavoriteStore(Long userId, Long storeId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User not found."));
        storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND, "Store not found."));

        if (!userFavoriteStoreRepository.existsByUserIdAndStoreId(userId, storeId)) {
            log.info("Store {} is not favorited by user {}", storeId, userId);
            return;
        }

        // 使用新添加的方法直接删除
        userFavoriteStoreRepository.deleteByUserIdAndStoreId(userId, storeId);
        log.info("User {} unfavorited store {}", userId, storeId);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean isStoreFavorite(Long userId, Long storeId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User not found."));
        storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND, "Store not found."));
        return userFavoriteStoreRepository.existsByUserIdAndStoreId(userId, storeId);
    }

    private HomePageResDTO.NearbyStoreDTO mapToNearbyStoreDTO(StoreEntity entity, BigDecimal userLat, BigDecimal userLon, Long userId) {
        HomePageResDTO.NearbyStoreDTO dto = new HomePageResDTO.NearbyStoreDTO();
        dto.setId(String.valueOf(entity.getId()));
        dto.setName(entity.getName());
        dto.setAddress(entity.getAddress());

        double distance = GeoUtil.calculateDistance(
                userLat.doubleValue(), userLon.doubleValue(),
                entity.getLatitude().doubleValue(), entity.getLongitude().doubleValue()
        );
        dto.setDistance((int) Math.round(distance));

        dto.setBusinessHours(entity.getBusinessHours());
        dto.setStatus(entity.getStatus());
        dto.setPhone(entity.getPhone());
        dto.setServices(entity.getServices().stream().map(StoreServiceEntity::getServiceType).collect(Collectors.toList()));
        dto.setTags(entity.getTags());
        dto.setDeliveryFee(entity.getDeliveryFee());
        dto.setMinimumOrderAmount(entity.getMinimumOrderAmount());
        dto.setRating(entity.getRating());
        dto.setImages(entity.getImages().stream().map(StoreImageEntity::getImageUrl).collect(Collectors.toList()));
        dto.setCurrentWaitTime(entity.getCurrentWaitTime());
        dto.setIsFavorite(userId != null && userFavoriteStoreRepository.existsByUserIdAndStoreId(userId, entity.getId()));

        return dto;
    }

    private StoreDetailResDTO mapToStoreDetailResDTO(StoreEntity entity, BigDecimal userLat, BigDecimal userLon, Long userId) {
        // StoreDetailResDTO extends HomePageResDTO.NearbyStoreDTO, so we can reuse the mapping logic
        // Then convert to StoreDetailResDTO
        HomePageResDTO.NearbyStoreDTO nearbyDto = mapToNearbyStoreDTO(entity, userLat, userLon, userId);
        StoreDetailResDTO detailDto = new StoreDetailResDTO();
        // Copy properties from nearbyDto to detailDto
        detailDto.setId(nearbyDto.getId());
        detailDto.setName(nearbyDto.getName());
        detailDto.setAddress(nearbyDto.getAddress());
        detailDto.setDistance(nearbyDto.getDistance());
        detailDto.setBusinessHours(nearbyDto.getBusinessHours());
        detailDto.setStatus(nearbyDto.getStatus());
        detailDto.setPhone(nearbyDto.getPhone());
        detailDto.setServices(nearbyDto.getServices());
        detailDto.setTags(nearbyDto.getTags());
        detailDto.setDeliveryFee(nearbyDto.getDeliveryFee());
        detailDto.setMinimumOrderAmount(nearbyDto.getMinimumOrderAmount());
        detailDto.setRating(nearbyDto.getRating());
        detailDto.setImages(nearbyDto.getImages());
        detailDto.setCurrentWaitTime(nearbyDto.getCurrentWaitTime());
        detailDto.setIsFavorite(nearbyDto.getIsFavorite());
        detailDto.setLongitude(entity.getLongitude()); // Add specific fields if any
        detailDto.setLatitude(entity.getLatitude()); // Add specific fields if any
        return detailDto;
    }
}