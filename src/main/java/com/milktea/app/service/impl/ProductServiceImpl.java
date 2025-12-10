// File: milktea-backend/src/main/java/com.milktea.app.service/impl/ProductServiceImpl.java
package com.milktea.app.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.milktea.app.common.constant.ErrorCode;
import com.milktea.app.common.exception.BusinessException;
import com.milktea.app.dto.product.ProductDetailResDTO;
import com.milktea.app.dto.product.ProductFavoriteStatusResDTO;
import com.milktea.app.dto.product.ProductListReqDTO;
import com.milktea.app.dto.product.ProductListResDTO;
import com.milktea.app.entity.*;
import com.milktea.app.repository.*;
import com.milktea.app.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductCustomizationTypeRepository customizationTypeRepository;
    private final ProductCustomizationOptionRepository customizationOptionRepository;
    private final ProductNutritionRepository productNutritionRepository;
    private final UserFavoriteProductRepository userFavoriteProductRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final AllergenRepository allergenRepository;
    private final IngredientRepository ingredientRepository;

    @Override
    @Transactional(readOnly = true)
    public ProductListResDTO getProducts(ProductListReqDTO reqDTO, Pageable pageable) {
        // 构建排序
        Sort sort = Sort.unsorted();
        if (reqDTO.getSort() != null) {
            switch (reqDTO.getSort()) {
                case "sales":
                    sort = Sort.by(Sort.Direction.DESC, "sales");
                    break;
                case "price_asc":
                    sort = Sort.by(Sort.Direction.ASC, "price");
                    break;
                case "price_desc":
                    sort = Sort.by(Sort.Direction.DESC, "price");
                    break;
                case "newest":
                    sort = Sort.by(Sort.Direction.DESC, "createdAt");
                    break;
                case "relevance":
                default:
                    sort = Sort.by(Sort.Direction.DESC, "rating");
                    break;
            }
        }

        // 创建新的 Pageable 对象，包含排序
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        // 构建 Specification - 简化版本
        Specification<ProductEntity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 只查询活跃产品
            predicates.add(criteriaBuilder.equal(root.get("isActive"), true));

            // 如果有其他过滤器
            if (reqDTO.getFilter() != null && !reqDTO.getFilter().isEmpty()) {
                switch (reqDTO.getFilter()) {
                    case "sugar_free":
                        predicates.add(criteriaBuilder.like(root.get("tags"), "%\"sugar_free\"%"));
                        break;
                    case "hot":
                        predicates.add(criteriaBuilder.equal(root.get("isHot"), true));
                        break;
                    case "ice":
                        // 这里可能需要更复杂的逻辑
                        break;
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 使用 Specification 查询
        Page<ProductEntity> productPage = productRepository.findAll(spec, sortedPageable);

        // 转换为 DTO
        List<ProductListResDTO.ProductItemDTO> productItemDTOs = productPage.getContent().stream()
                .map(this::mapToProductItemDTO)
                .collect(Collectors.toList());

        // 构建响应
        ProductListResDTO resDTO = new ProductListResDTO();
        resDTO.setProducts(productItemDTOs);
        resDTO.setTotal((int) productPage.getTotalElements());
        resDTO.setPage(productPage.getNumber() + 1);
        resDTO.setLimit(sortedPageable.getPageSize());

        return resDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductListResDTO getCategoryProducts(Long categoryId, ProductListReqDTO reqDTO, Pageable pageable) {
        // 验证分类是否存在
        CategoryEntity category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Category not found."));

        // 构建排序
        Sort sort = Sort.unsorted();
        if (reqDTO.getSort() != null) {
            switch (reqDTO.getSort()) {
                case "sales":
                    sort = Sort.by(Sort.Direction.DESC, "sales");
                    break;
                case "price_asc":
                    sort = Sort.by(Sort.Direction.ASC, "price");
                    break;
                case "price_desc":
                    sort = Sort.by(Sort.Direction.DESC, "price");
                    break;
                case "newest":
                    sort = Sort.by(Sort.Direction.DESC, "createdAt");
                    break;
                default:
                    break;
            }
        }

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        // 构建 Specification - 简化版本
        Specification<ProductEntity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 只查询活跃产品
            predicates.add(criteriaBuilder.equal(root.get("isActive"), true));

            // 按分类筛选
            predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));

            // 如果有过滤器
            if (reqDTO.getFilter() != null && !reqDTO.getFilter().isEmpty()) {
                switch (reqDTO.getFilter()) {
                    case "sugar_free":
                        predicates.add(criteriaBuilder.like(root.get("tags"), "%\"sugar_free\"%"));
                        break;
                    case "hot":
                        predicates.add(criteriaBuilder.equal(root.get("isHot"), true));
                        break;
                    case "ice":
                        // 这里可能需要更复杂的逻辑
                        break;
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 查询
        Page<ProductEntity> productPage = productRepository.findAll(spec, sortedPageable);

        // 转换为 DTO
        List<ProductListResDTO.ProductItemDTO> productItemDTOs = productPage.getContent().stream()
                .map(this::mapToProductItemDTO)
                .collect(Collectors.toList());

        // 构建响应
        ProductListResDTO resDTO = new ProductListResDTO();
        resDTO.setProducts(productItemDTOs);
        resDTO.setTotal((int) productPage.getTotalElements());
        resDTO.setPage(productPage.getNumber() + 1);
        resDTO.setLimit(sortedPageable.getPageSize());

        return resDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResDTO getProductDetail(Long userId, Long productId) {
        // Fetch product with eager loading for related entities to avoid N+1 problem
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found."));

        if (!product.getIsActive()) {
            throw new BusinessException(ErrorCode.PRODUCT_OFFLINE, "Product is offline.");
        }

        ProductDetailResDTO dto = new ProductDetailResDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSubtitle(product.getSubtitle());
        dto.setMainImage(product.getMainImageUrl());
        dto.setPrice(product.getPrice());
        dto.setOriginalPrice(product.getOriginalPrice());
        dto.setUnit(product.getUnit());
        dto.setStock(product.getStock());
        dto.setSales(product.getSales());
        dto.setMonthlySales(product.getMonthlySales());
        dto.setRating(product.getRating());
        dto.setRatingCount(product.getRatingCount());
        dto.setFavoriteCount(product.getFavoriteCount());
        dto.setIsHot(product.getIsHot());
        dto.setIsNew(product.getIsNew());
        dto.setIsRecommend(product.getIsRecommend());
        dto.setDescription(product.getDescription());
        dto.setDetailHtml(product.getDetailHtml());
        dto.setStorage(product.getStorageMethod());
        dto.setShelfLife(product.getShelfLife());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());

        // Category info
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        // Images
        List<ProductImageEntity> images = productImageRepository.findByProductIdOrderBySortOrderAsc(productId);
        dto.setImages(images.stream().map(ProductImageEntity::getImageUrl).collect(Collectors.toList()));

        // Tags
        if (product.getTags() != null) {
            try {
                dto.setTags(objectMapper.readValue(product.getTags(), new TypeReference<List<String>>() {}));
            } catch (Exception e) {
                log.error("Failed to parse product tags for product {}: {}", product.getId(), e.getMessage());
                dto.setTags(new ArrayList<>());
            }
        } else {
            dto.setTags(new ArrayList<>());
        }

        // Customizations
        ProductDetailResDTO.CustomizationsDTO customizationsDTO = new ProductDetailResDTO.CustomizationsDTO();
        List<ProductCustomizationTypeEntity> customizationTypes = customizationTypeRepository.findByProductIdAndIsEnabledTrueOrderBySortOrderAsc(productId);

        for (ProductCustomizationTypeEntity type : customizationTypes) {
            // 使用正确的方法名
            List<ProductCustomizationOptionEntity> options = customizationOptionRepository.findByCustomizationTypeIdOrderBySortOrderAsc(type.getId());

            List<ProductDetailResDTO.OptionDTO> optionDTOs = options.stream()
                    .map(option -> new ProductDetailResDTO.OptionDTO(
                            option.getValue(),
                            option.getLabel(),
                            option.getPriceAdjustment(),
                            option.getIsDefault()
                    )).collect(Collectors.toList());

            if ("sweetness".equals(type.getTypeName())) {
                customizationsDTO.setSweetness(new ProductDetailResDTO.CustomizationTypeDTO(
                        type.getIsEnabled(), type.getIsRequired(), optionDTOs
                ));
            } else if ("temperature".equals(type.getTypeName())) {
                customizationsDTO.setTemperature(new ProductDetailResDTO.CustomizationTypeDTO(
                        type.getIsEnabled(), type.getIsRequired(), optionDTOs
                ));
            } else if ("toppings".equals(type.getTypeName())) {
                List<ProductDetailResDTO.ToppingOptionDTO> toppingOptionDTOs = options.stream()
                        .map(option -> new ProductDetailResDTO.ToppingOptionDTO(
                                String.valueOf(option.getId()), // 将 Long 转换为 String
                                option.getLabel(),
                                option.getPriceAdjustment(),
                                option.getStock(),
                                option.getIconUrl()
                        )).collect(Collectors.toList());
                customizationsDTO.setToppings(new ProductDetailResDTO.ToppingsCustomizationDTO(
                        type.getIsEnabled(), type.getIsRequired(), type.getMaxQuantity(), toppingOptionDTOs
                ));
            }
        }
        dto.setCustomizations(customizationsDTO);

        // Nutrition
        List<ProductNutritionEntity> nutritions = productNutritionRepository.findByProductId(productId);
        dto.setNutrition(nutritions.stream()
                .map(n -> new ProductDetailResDTO.NutritionDTO(n.getName(), n.getValue(), n.getUnit()))
                .collect(Collectors.toList()));

        // Ingredients & Allergens
        dto.setIngredients(product.getIngredients().stream().map(IngredientEntity::getName).collect(Collectors.toList()));
        dto.setAllergens(product.getAllergens().stream().map(AllergenEntity::getName).collect(Collectors.toList()));

        // Related Products
        dto.setRelatedProducts(product.getRelatedProducts().stream()
                .map(rp -> new ProductDetailResDTO.RelatedProductDTO(
                        rp.getId(), // 这里使用 Long 类型
                        rp.getName(),
                        rp.getMainImageUrl(),
                        rp.getPrice()))
                .collect(Collectors.toList()));

        return dto;
    }

    @Override
    @Transactional
    public void addFavoriteProduct(Long userId, Long productId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User not found."));
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found."));

        if (userFavoriteProductRepository.existsByUserIdAndProductId(userId, productId)) {
            log.info("Product {} is already favorited by user {}", productId, userId);
            return;
        }

        UserFavoriteProductEntity favorite = new UserFavoriteProductEntity();
        favorite.setUser(user);
        favorite.setProduct(product);
        favorite.setCreatedAt(Instant.now());
        userFavoriteProductRepository.save(favorite);

        // Increment product's favorite count
        product.setFavoriteCount(product.getFavoriteCount() + 1);
        productRepository.save(product);
        log.info("User {} favorited product {}", userId, productId);
    }

    @Override
    @Transactional
    public void removeFavoriteProduct(Long userId, Long productId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User not found."));
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found."));

        Optional<UserFavoriteProductEntity> favoriteOptional = userFavoriteProductRepository.findByUserIdAndProductId(userId, productId);
        if (favoriteOptional.isEmpty()) {
            log.info("Product {} is not favorited by user {}", productId, userId);
            return;
        }

        userFavoriteProductRepository.delete(favoriteOptional.get());

        // Decrement product's favorite count
        product.setFavoriteCount(product.getFavoriteCount() - 1);
        productRepository.save(product);
        log.info("User {} unfavorited product {}", userId, productId);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductFavoriteStatusResDTO getProductFavoriteStatus(Long userId, Long productId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User not found."));
        productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found."));

        Optional<UserFavoriteProductEntity> favorite = userFavoriteProductRepository.findByUserIdAndProductId(userId, productId);

        return new ProductFavoriteStatusResDTO(
                favorite.isPresent(),
                favorite.map(UserFavoriteProductEntity::getId).orElse(null)
        );
    }

    private ProductListResDTO.ProductItemDTO mapToProductItemDTO(ProductEntity entity) {
        ProductListResDTO.ProductItemDTO dto = new ProductListResDTO.ProductItemDTO();
        dto.setId(String.valueOf(entity.getId())); // 修复：将 Long 转换为 String
        dto.setName(entity.getName());
        dto.setImage(entity.getMainImageUrl());
        dto.setPrice(entity.getPrice());
        dto.setOriginalPrice(entity.getOriginalPrice());
        dto.setSales(entity.getSales());
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
}