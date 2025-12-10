package com.milktea.app.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.milktea.app.common.constant.ErrorCode;
import com.milktea.app.common.exception.BusinessException;
import com.milktea.app.common.util.GeoUtil;
import com.milktea.app.dto.home.HomePageResDTO;
import com.milktea.app.dto.product.ProductListResDTO;
import com.milktea.app.entity.*;
import com.milktea.app.repository.*;
import com.milktea.app.service.HomeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HomeServiceImpl implements HomeService {

    private final BannerRepository bannerRepository;
    private final QuickEntryRepository quickEntryRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final PromotionRepository promotionRepository;
    private final StoreRepository storeRepository;
    private final UserFavoriteStoreRepository userFavoriteStoreRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public HomePageResDTO getHomePageData(BigDecimal latitude, BigDecimal longitude) {
        HomePageResDTO homePageResDTO = new HomePageResDTO();

        // 1. Banners
        List<BannerEntity> bannerEntities = bannerRepository.findByIsActiveTrueOrderBySortOrderAsc();
        homePageResDTO.setBanners(bannerEntities.stream()
                .map(this::mapToBannerDTO)
                .collect(Collectors.toList()));

        // 2. Quick Entries
        List<QuickEntryEntity> quickEntryEntities = quickEntryRepository.findByIsActiveTrueOrderBySortOrderAsc();
        homePageResDTO.setQuickEntries(quickEntryEntities.stream()
                .map(this::mapToQuickEntryDTO)
                .collect(Collectors.toList()));

        // 3. Recommend Products (e.g., is_recommend=true)
        List<ProductEntity> recommendProducts = productRepository.findByIsRecommendTrueAndIsActiveTrue();
        homePageResDTO.setRecommendProducts(new HomePageResDTO.RecommendProductsDTO(
                "为你推荐",
                recommendProducts.stream()
                        .map(this::mapToProductItemDTO)
                        .collect(Collectors.toList())
        ));

        // 4. Categories (e.g., top-level active categories)
        List<CategoryEntity> categories = categoryRepository.findByParentIsNullAndIsActiveTrueOrderBySortOrderAsc();
        homePageResDTO.setCategories(categories.stream()
                .map(this::mapToCategoryDTO)
                .collect(Collectors.toList()));

        // 5. Hot Products (e.g., is_hot=true, ordered by monthly sales)
        List<ProductEntity> hotProducts = productRepository.findByIsHotTrueAndIsActiveTrueOrderByMonthlySalesDesc();
        homePageResDTO.setHotProducts(hotProducts.stream()
                .limit(5) // Limit to top 5 hot products
                .map(this::mapToHotProductDTO)
                .collect(Collectors.toList()));

        // 6. Promotions
        List<PromotionEntity> promotionEntities = promotionRepository.findByIsActiveTrueAndEndTimeAfterOrderByStartTimeAsc(Instant.now());
        homePageResDTO.setPromotions(promotionEntities.stream()
                .map(this::mapToPromotionDTO)
                .collect(Collectors.toList()));

        // 7. Nearby Store (Requires latitude and longitude)
        if (latitude != null && longitude != null) {
            List<StoreEntity> nearbyStores = storeRepository.findNearbyStoresNative(latitude, longitude, 5000.0, 1); // 5km radius, top 1
            if (!nearbyStores.isEmpty()) {
                StoreEntity nearestStore = nearbyStores.get(0);
                homePageResDTO.setNearbyStore(mapToNearbyStoreDTO(nearestStore, latitude, longitude, null)); // userId is null for public homepage
            } else {
                homePageResDTO.setNearbyStore(null); // No nearby store found
            }
        } else {
            homePageResDTO.setNearbyStore(null); // No location provided
        }

        return homePageResDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductListResDTO getRecommendedProducts(Integer limit, String type) {
        List<ProductEntity> recommendedProducts;
        // Placeholder for more sophisticated recommendation logic based on 'type'
        if ("category".equalsIgnoreCase(type)) {
            // Example: recommend from a specific category (e.g. ID 1 for Milk Tea)
            recommendedProducts = productRepository.findByCategoryIdAndIsActiveTrue(1L);
        } else if ("combo".equalsIgnoreCase(type)) {
            // Example: recommend combo products (assuming a tag or flag for combos)
            recommendedProducts = productRepository.findByIsActiveTrue(PageRequest.of(0, limit)).getContent().stream()
                    .filter(p -> p.getTags() != null && p.getTags().contains("combo")) // Simplified
                    .collect(Collectors.toList());
        } else { // Default or "product" type
            recommendedProducts = productRepository.findByIsRecommendTrueAndIsActiveTrue();
        }

        // 修改这里：使用新的映射方法 mapToProductListItemDTO
        List<ProductListResDTO.ProductItemDTO> productItemDTOs = recommendedProducts.stream()
                .limit(limit != null ? limit : 10)
                .map(this::mapToProductListItemDTO)  // 使用新的映射方法
                .collect(Collectors.toList());

        ProductListResDTO resDTO = new ProductListResDTO();
        resDTO.setProducts(productItemDTOs);
        resDTO.setTotal(productItemDTOs.size()); // Total for this specific recommendation list
        resDTO.setPage(1);
        resDTO.setLimit(limit != null ? limit : 10);
        return resDTO;
    }


    private HomePageResDTO.BannerDTO mapToBannerDTO(BannerEntity entity) {
        HomePageResDTO.BannerDTO dto = new HomePageResDTO.BannerDTO();
        dto.setId(String.valueOf(entity.getId()));
        dto.setImage(entity.getImageUrl());
        dto.setTitle(entity.getTitle());
        dto.setSubtitle(entity.getSubtitle());
        dto.setType(entity.getType());
        dto.setTargetId(entity.getTargetId());
        dto.setUrl(entity.getUrl());
        dto.setBackgroundColor(entity.getBackgroundColor());
        return dto;
    }

    private HomePageResDTO.QuickEntryDTO mapToQuickEntryDTO(QuickEntryEntity entity) {
        HomePageResDTO.QuickEntryDTO dto = new HomePageResDTO.QuickEntryDTO();
        dto.setId(String.valueOf(entity.getId()));
        dto.setIcon(entity.getIconUrl());
        dto.setName(entity.getName());
        dto.setType(entity.getType());
        dto.setTargetId(String.valueOf(entity.getTargetId()));
        dto.setBadge(entity.getBadge());
        return dto;
    }

    private HomePageResDTO.ProductItemDTO mapToProductItemDTO(ProductEntity entity) {
        HomePageResDTO.ProductItemDTO dto = new HomePageResDTO.ProductItemDTO();
        dto.setId(String.valueOf(entity.getId()));
        dto.setName(entity.getName());
        dto.setImage(entity.getMainImageUrl());
        dto.setPrice(entity.getPrice());
        dto.setOriginalPrice(entity.getOriginalPrice());
        dto.setSales(entity.getSales());
        // Parse tags from JSONB string
        if (entity.getTags() != null) {
            try {
                dto.setTags(objectMapper.readValue(entity.getTags(), new TypeReference<List<String>>() {}));
            } catch (Exception e) {
                log.error("Failed to parse product tags for product {}: {}", entity.getId(), e.getMessage());
                dto.setTags(new ArrayList<>());
            }
        } else {
            dto.setTags(new ArrayList<>());
        }
        dto.setDescription(entity.getDescription());
        return dto;
    }

    // 新增方法：映射到 ProductListResDTO.ProductItemDTO
    private ProductListResDTO.ProductItemDTO mapToProductListItemDTO(ProductEntity entity) {
        ProductListResDTO.ProductItemDTO dto = new ProductListResDTO.ProductItemDTO();
        dto.setId(String.valueOf(entity.getId()));
        dto.setName(entity.getName());
        dto.setImage(entity.getMainImageUrl());
        dto.setPrice(entity.getPrice());
        dto.setOriginalPrice(entity.getOriginalPrice());
        dto.setSales(entity.getSales());
        // Parse tags from JSONB string
        if (entity.getTags() != null) {
            try {
                dto.setTags(objectMapper.readValue(entity.getTags(), new TypeReference<List<String>>() {}));
            } catch (Exception e) {
                log.error("Failed to parse product tags for product {}: {}", entity.getId(), e.getMessage());
                dto.setTags(new ArrayList<>());
            }
        } else {
            dto.setTags(new ArrayList<>());
        }
        dto.setDescription(entity.getDescription());
        return dto;
    }

    private HomePageResDTO.CategoryDTO mapToCategoryDTO(CategoryEntity entity) {
        HomePageResDTO.CategoryDTO dto = new HomePageResDTO.CategoryDTO();
        dto.setId(String.valueOf(entity.getId()));
        dto.setName(entity.getName());
        dto.setIcon(entity.getIconUrl());
        dto.setImage(entity.getImageUrl());
        // Product count would need a separate query or cached value
        dto.setProductCount(0); // Placeholder
        dto.setDescription(entity.getDescription());
        return dto;
    }

    private HomePageResDTO.HotProductDTO mapToHotProductDTO(ProductEntity entity) {
        HomePageResDTO.HotProductDTO dto = new HomePageResDTO.HotProductDTO();
        dto.setId(String.valueOf(entity.getId()));
        dto.setRank(0); // Placeholder for actual ranking logic
        dto.setName(entity.getName());
        dto.setImage(entity.getMainImageUrl());
        dto.setPrice(entity.getPrice());
        dto.setRating(entity.getRating());
        dto.setMonthlySales(entity.getMonthlySales());
        dto.setIncreaseRate(BigDecimal.valueOf(0.15)); // Placeholder
        return dto;
    }

    private HomePageResDTO.PromotionDTO mapToPromotionDTO(PromotionEntity entity) {
        HomePageResDTO.PromotionDTO dto = new HomePageResDTO.PromotionDTO();
        dto.setId(String.valueOf(entity.getId()));
        dto.setTitle(entity.getTitle());
        dto.setSubtitle(entity.getSubtitle());
        dto.setImage(entity.getImageUrl());
        dto.setType(entity.getType());
        dto.setValue(entity.getValue());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setButtonText(entity.getButtonText());
        return dto;
    }

    private HomePageResDTO.NearbyStoreDTO mapToNearbyStoreDTO(StoreEntity entity, BigDecimal userLat, BigDecimal userLon, Long userId) {
        HomePageResDTO.NearbyStoreDTO dto = new HomePageResDTO.NearbyStoreDTO();
        dto.setId(String.valueOf(entity.getId()));
        dto.setName(entity.getName());
        dto.setAddress(entity.getAddress());
        // Calculate distance in meters
        double distance = GeoUtil.calculateDistance(
                userLat.doubleValue(), userLon.doubleValue(),
                entity.getLatitude().doubleValue(), entity.getLongitude().doubleValue()
        );
        dto.setDistance((int) Math.round(distance));
        dto.setBusinessHours(entity.getBusinessHours());
        dto.setStatus(entity.getStatus());
        dto.setPhone(entity.getPhone());
        // Services and Tags require mapping from related entities
        dto.setServices(entity.getServices().stream().map(StoreServiceEntity::getServiceType).collect(Collectors.toList()));
        dto.setTags(entity.getTags());
        dto.setDeliveryFee(entity.getDeliveryFee());
        dto.setMinimumOrderAmount(entity.getMinimumOrderAmount());
        dto.setRating(entity.getRating());
        dto.setImages(entity.getImages().stream().map(StoreImageEntity::getImageUrl).collect(Collectors.toList()));
        dto.setCurrentWaitTime(entity.getCurrentWaitTime());
        // Set isFavorite if userId is provided
        dto.setIsFavorite(userId != null && userFavoriteStoreRepository.existsByUserIdAndStoreId(userId, entity.getId()));
        dto.setLongitude(entity.getLongitude()); // Added from StoreDetailResDTO
        dto.setLatitude(entity.getLatitude());   // Added from StoreDetailResDTO
        return dto;
    }
}