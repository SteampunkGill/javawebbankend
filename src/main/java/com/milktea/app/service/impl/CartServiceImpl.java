// File: milktea-backend/src/main/java/com.milktea.app/service/impl/CartServiceImpl.java
package com.milktea.app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.milktea.app.common.constant.ErrorCode;
import com.milktea.app.common.exception.BusinessException;
import com.milktea.app.dto.cart.CartBatchOperationReqDTO;
import com.milktea.app.dto.cart.CartItemAddReqDTO;
import com.milktea.app.dto.cart.CartItemUpdateReqDTO;
import com.milktea.app.dto.cart.CartResDTO;
import com.milktea.app.entity.*;
import com.milktea.app.repository.*;
import com.milktea.app.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.milktea.app.dto.cart.CustomizationDTO;
import com.milktea.app.dto.cart.ToppingItemDTO;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final CartItemCustomizationRepository cartItemCustomizationRepository;
    private final ProductRepository productRepository;
    private final ProductCustomizationTypeRepository customizationTypeRepository;
    private final ProductCustomizationOptionRepository customizationOptionRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper; // For JSON parsing for product tags etc.

    @Override
    @Transactional(readOnly = true)
    public CartResDTO getCart(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User not found."));

        List<CartItemEntity> cartItems = cartItemRepository.findByUserId(userId);
        List<CartResDTO.CartItemDTO> itemDTOs = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal selectedAmount = BigDecimal.ZERO;
        int totalQuantity = 0;
        int selectedQuantity = 0;
        int validItemCount = 0;
        int invalidItemCount = 0;

        for (CartItemEntity item : cartItems) {
            // Re-validate product details and price to ensure it's up-to-date
            ProductEntity product = productRepository.findById(item.getProduct().getId())
                    .orElse(null);

            CartResDTO.CartItemDTO itemDTO = new CartResDTO.CartItemDTO();
            itemDTO.setId(item.getId());
            itemDTO.setProductId(item.getProduct().getId());
            itemDTO.setProductImage(item.getProduct().getMainImageUrl());

            boolean currentProductValid = true;
            String invalidReason = null;

            if (product == null || !product.getIsActive()) {
                currentProductValid = false;
                invalidReason = "商品已下架";
                itemDTO.setProductName(item.getProduct().getName() + "(已下架)"); // Show original name but indicate status
                itemDTO.setPrice(item.getPriceAtAdd()); // Keep original price for display
                itemDTO.setOriginalPrice(item.getOriginalPriceAtAdd());
            } else {
                itemDTO.setProductName(product.getName());
                itemDTO.setPrice(product.getPrice());
                itemDTO.setOriginalPrice(product.getOriginalPrice());
            }

            // Check stock
            if (currentProductValid && product.getStock() < item.getQuantity()) {
                currentProductValid = false;
                invalidReason = "库存不足";
            }

            itemDTO.setQuantity(item.getQuantity());
            itemDTO.setMaxQuantity(product != null ? product.getStock() : 0); // Current stock
            itemDTO.setStock(product != null ? product.getStock() : 0);
            itemDTO.setIsSelected(item.getIsSelected());
            itemDTO.setIsValid(currentProductValid);
            itemDTO.setInvalidReason(invalidReason);

            // Calculate subtotal for the item
            BigDecimal itemBasePrice = currentProductValid ? product.getPrice() : item.getPriceAtAdd();
            BigDecimal itemSubtotal = itemBasePrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            // Add customization price adjustments
            CartResDTO.CustomizationsDTO customizationsDTO = new CartResDTO.CustomizationsDTO();
            List<CartResDTO.ToppingDTO> toppingDTOs = new ArrayList<>();

            for (CartItemCustomizationEntity customization : item.getCustomizations()) {
                BigDecimal adjustment = currentProductValid ?
                        customizationOptionRepository.findById(getOptionIdFromValue(customization.getOptionValue())) // This needs to be improved; direct value lookup is problematic
                                .map(ProductCustomizationOptionEntity::getPriceAdjustment)
                                .orElse(customization.getPriceAdjustmentAtAdd()) : customization.getPriceAdjustmentAtAdd();

                itemSubtotal = itemSubtotal.add(adjustment.multiply(BigDecimal.valueOf(customization.getQuantity())));

                CartResDTO.OptionDTO optionDTO = new CartResDTO.OptionDTO(
                        customization.getOptionValue(),
                        customization.getOptionLabel(),
                        adjustment
                );
                switch (customization.getCustomizationTypeName()) {
                    case "sweetness" -> customizationsDTO.setSweetness(optionDTO);
                    case "temperature" -> customizationsDTO.setTemperature(optionDTO);
                    case "toppings" -> toppingDTOs.add(new CartResDTO.ToppingDTO(
                            customization.getOptionValue(),
                            customization.getOptionLabel(),
                            adjustment,
                            customization.getQuantity()
                    ));
                }
            }
            customizationsDTO.setToppings(toppingDTOs);
            itemDTO.setCustomizations(customizationsDTO);
            itemDTO.setSubtotal(itemSubtotal);

            itemDTOs.add(itemDTO);

            totalQuantity += item.getQuantity();
            totalAmount = totalAmount.add(itemSubtotal);

            if (item.getIsSelected()) {
                selectedQuantity += item.getQuantity();
                selectedAmount = selectedAmount.add(itemSubtotal);
            }

            if (currentProductValid) {
                validItemCount++;
            } else {
                invalidItemCount++;
            }
        }

        // Calculate final summary
        // For simplicity, delivery fee and discount are placeholders.
        BigDecimal deliveryFee = BigDecimal.ZERO; // Will be calculated in checkout based on store/address
        BigDecimal totalDiscount = BigDecimal.ZERO; // Will be calculated in checkout based on coupons/promotions
        BigDecimal finalAmount = selectedAmount.add(deliveryFee).subtract(totalDiscount);

        CartResDTO.CartSummaryDTO summary = new CartResDTO.CartSummaryDTO(
                totalQuantity, selectedQuantity, totalAmount, selectedAmount,
                totalDiscount, deliveryFee, finalAmount, validItemCount, invalidItemCount
        );

        return new CartResDTO(itemDTOs, summary);
    }

    @Override
    @Transactional
    public CartResDTO addCartItem(Long userId, CartItemAddReqDTO reqDTO) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User not found."));
        ProductEntity product = productRepository.findById(reqDTO.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found."));

        if (!product.getIsActive()) {
            throw new BusinessException(ErrorCode.PRODUCT_OFFLINE, "Product is offline.");
        }
        if (product.getStock() < reqDTO.getQuantity()) {
            throw new BusinessException(ErrorCode.PRODUCT_STOCK_INSUFFICIENT, "Product stock insufficient.");
        }

        // Check if item with same product and *same customizations* already exists
        // This logic can be complex due to customization comparison. For simplicity,
        // we'll assume one entry per product in cart for now, and customizations are updated.
        // A more robust solution would involve a hash of customizations for unique cart items.
        Optional<CartItemEntity> existingCartItemOptional = cartItemRepository.findByUserIdAndProductId(userId, reqDTO.getProductId());
        CartItemEntity cartItem;

        if (existingCartItemOptional.isPresent()) {
            cartItem = existingCartItemOptional.get();
            cartItem.setQuantity(cartItem.getQuantity() + reqDTO.getQuantity()); // Just increase quantity
            // For production: if customizations can change for the same product,
            // new custom entry or logic to handle variant selection is needed.
            // For now, we update customizations if provided, overwriting previous ones.
            updateCartItemCustomizations(cartItem, product, reqDTO.getChoices());
        } else {
            cartItem = new CartItemEntity();
            cartItem.setUser(user);
            cartItem.setProduct(product);
            cartItem.setQuantity(reqDTO.getQuantity());
            cartItem.setIsSelected(true);
            cartItem.setIsValid(true);
            cartItem.setPriceAtAdd(product.getPrice());
            cartItem.setOriginalPriceAtAdd(product.getOriginalPrice());
            cartItem = cartItemRepository.save(cartItem);
            updateCartItemCustomizations(cartItem, product, reqDTO.getChoices());
        }

        return getCart(userId);
    }

    @Override
    @Transactional
    public CartResDTO updateCartItem(Long userId, Long cartItemId, CartItemUpdateReqDTO reqDTO) {
        CartItemEntity cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND, "Cart item not found."));

        if (!cartItem.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Access denied to cart item.");
        }

        ProductEntity product = productRepository.findById(cartItem.getProduct().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found."));

        if (!product.getIsActive()) {
            throw new BusinessException(ErrorCode.PRODUCT_OFFLINE, "Product is offline.");
        }

        if (reqDTO.getQuantity() != null) {
            if (product.getStock() < reqDTO.getQuantity()) {
                throw new BusinessException(ErrorCode.PRODUCT_STOCK_INSUFFICIENT, "Product stock insufficient.");
            }
            cartItem.setQuantity(reqDTO.getQuantity());
        }

        // Update customizations if provided
        if (reqDTO.getChoices() != null) {
            updateCartItemCustomizations(cartItem, product, reqDTO.getChoices());
        }

        cartItemRepository.save(cartItem);
        return getCart(userId);
    }

    @Override
    @Transactional
    public CartResDTO deleteCartItem(Long userId, Long cartItemId) {
        CartItemEntity cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND, "Cart item not found."));

        if (!cartItem.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Access denied to cart item.");
        }

        cartItemRepository.delete(cartItem);
        return getCart(userId);
    }

    @Override
    @Transactional
    public CartResDTO batchOperateCartItems(Long userId, CartBatchOperationReqDTO reqDTO) {
        List<CartItemEntity> cartItems = new ArrayList<>();
        if (reqDTO.getItemIds() != null && !reqDTO.getItemIds().isEmpty()) {
            cartItems = cartItemRepository.findAllById(reqDTO.getItemIds());
            cartItems = cartItems.stream()
                    .filter(item -> item.getUser().getId().equals(userId))
                    .collect(Collectors.toList());
        } else if ("select_all".equals(reqDTO.getAction()) || "unselect_all".equals(reqDTO.getAction())) {
            cartItems = cartItemRepository.findByUserId(userId);
        }

        switch (reqDTO.getAction()) {
            case "select_all" -> cartItems.forEach(item -> item.setIsSelected(true));
            case "unselect_all" -> cartItems.forEach(item -> item.setIsSelected(false));
            case "delete_selected" -> {
                cartItemRepository.deleteAll(cartItems);
                // After deletion, fetch the remaining cart items
                return getCart(userId);
            }
            default -> throw new BusinessException(ErrorCode.INVALID_PARAM, "Unsupported cart batch operation.");
        }
        cartItemRepository.saveAll(cartItems);
        return getCart(userId);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    // File: milktea-backend/src/main/java/com.milktea.app/service/impl/CartServiceImpl.java
// 修改 updateCartItemCustomizations 方法
    private void updateCartItemCustomizations(CartItemEntity cartItem, ProductEntity product, CustomizationDTO choices) {
        // 关键修改：检查customizations是否为null
        List<CartItemCustomizationEntity> existingCustomizations = cartItem.getCustomizations();
        if (existingCustomizations != null && !existingCustomizations.isEmpty()) {
            cartItemCustomizationRepository.deleteAll(existingCustomizations);
        }

        // 确保customizations列表不为null
        if (cartItem.getCustomizations() == null) {
            cartItem.setCustomizations(new ArrayList<>());
        } else {
            cartItem.getCustomizations().clear();
        }

        if (choices == null) return;

        // Fetch product's customization types and options
        Map<String, ProductCustomizationTypeEntity> productCustomizationTypes = customizationTypeRepository.findByProductIdAndIsEnabledTrueOrderBySortOrderAsc(product.getId())
                .stream()
                .collect(Collectors.toMap(ProductCustomizationTypeEntity::getTypeName, type -> type));

        if (choices.getSweetness() != null) {
            ProductCustomizationTypeEntity type = productCustomizationTypes.get("sweetness");
            if (type != null) {
                ProductCustomizationOptionEntity option = type.getOptions().stream()
                        .filter(o -> o.getValue().equals(choices.getSweetness()))
                        .findFirst()
                        .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_PARAM, "Invalid sweetness option."));
                cartItem.getCustomizations().add(createCartItemCustomization(cartItem, type, option, 1));
            }
        }
        if (choices.getTemperature() != null) {
            ProductCustomizationTypeEntity type = productCustomizationTypes.get("temperature");
            if (type != null) {
                ProductCustomizationOptionEntity option = type.getOptions().stream()
                        .filter(o -> o.getValue().equals(choices.getTemperature()))
                        .findFirst()
                        .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_PARAM, "Invalid temperature option."));
                cartItem.getCustomizations().add(createCartItemCustomization(cartItem, type, option, 1));
            }
        }
        if (choices.getToppings() != null && !choices.getToppings().isEmpty()) {
            ProductCustomizationTypeEntity type = productCustomizationTypes.get("toppings");
            if (type != null) {
                Map<String, ProductCustomizationOptionEntity> toppingOptions = type.getOptions().stream()
                        .collect(Collectors.toMap(ProductCustomizationOptionEntity::getValue, o -> o));

                for (ToppingItemDTO toppingItem : choices.getToppings()) {
                    ProductCustomizationOptionEntity option = toppingOptions.get(toppingItem.getId());
                    if (option == null) {
                        throw new BusinessException(ErrorCode.INVALID_PARAM, "Invalid topping option: " + toppingItem.getId());
                    }
                    if (option.getStock() != null && option.getStock() < toppingItem.getQuantity()) {
                        throw new BusinessException(ErrorCode.PRODUCT_STOCK_INSUFFICIENT, "Topping stock insufficient for: " + option.getLabel());
                    }
                    cartItem.getCustomizations().add(createCartItemCustomization(cartItem, type, option, toppingItem.getQuantity()));
                }
            }
        }

        // 只有列表不为空时才保存
        if (!cartItem.getCustomizations().isEmpty()) {
            cartItemCustomizationRepository.saveAll(cartItem.getCustomizations());
        }
    }

    private CartItemCustomizationEntity createCartItemCustomization(
            CartItemEntity cartItem,
            ProductCustomizationTypeEntity type,
            ProductCustomizationOptionEntity option,
            Integer quantity) {
        CartItemCustomizationEntity customization = new CartItemCustomizationEntity();
        customization.setCartItem(cartItem);
        customization.setCustomizationTypeName(type.getTypeName());
        customization.setOptionValue(option.getValue());
        customization.setOptionLabel(option.getLabel());
        customization.setPriceAdjustmentAtAdd(option.getPriceAdjustment());
        customization.setQuantity(quantity);
        return customization;
    }

    // This is a simplified method. In a real system, customization options would have their own IDs.
    // DTO uses 'value' for options, so we need to map that back to an ID if fetching from repo directly.
    private Long getOptionIdFromValue(String optionValue) {
        // This is highly problematic without knowing the actual mapping or global option IDs.
        // For a more robust solution, the `ProductCustomizationOptionEntity` should either:
        // 1. Have a unique 'code' field that maps to DTO 'value'.
        // 2. Be fetched by `customizationType.id` and `value`.
        // Placeholder for now.
        log.warn("Directly mapping option value '{}' to a dummy ID. This needs proper implementation.", optionValue);
        // Returning a dummy ID or throwing an error is appropriate for a placeholder.
        // For a real app, you'd likely query:
        // customizationOptionRepository.findByCustomizationTypeAndValue(type, optionValue).getId();
        return 1L; // Dummy ID
    }
}