package com.milktea.app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.milktea.app.common.constant.ErrorCode;
import com.milktea.app.common.enums.OrderStatus;
import com.milktea.app.common.enums.PayStatus;
import com.milktea.app.common.exception.BusinessException;
import com.milktea.app.common.util.DateUtil;
import com.milktea.app.common.util.GeoUtil;
import com.milktea.app.dto.coupon.CouponListResDTO;
import com.milktea.app.dto.home.HomePageResDTO;
import com.milktea.app.dto.order.*;
import com.milktea.app.dto.user.UserAddressResDTO;
import com.milktea.app.entity.*;
import com.milktea.app.repository.*;
import com.milktea.app.service.OrderService;
import com.milktea.app.controller.websocket.OrderWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemCustomizationRepository orderItemCustomizationRepository;
    private final OrderStatusTimelineRepository orderStatusTimelineRepository;
    private final PaymentRepository paymentRepository;
    private final OrderReviewRepository orderReviewRepository;
    private final OrderReviewImageRepository orderReviewImageRepository;
    private final OrderRefundRepository orderRefundRepository;
    private final OrderRefundImageRepository orderRefundImageRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserAddressRepository userAddressRepository;
    private final StoreRepository storeRepository;
    private final UserCouponRepository userCouponRepository;
    private final CouponTemplateRepository couponTemplateRepository;
    private final ProductCustomizationTypeRepository customizationTypeRepository;
    private final ProductCustomizationOptionRepository customizationOptionRepository;
    private final ReviewTagRepository reviewTagRepository;
    private final UserFavoriteStoreRepository userFavoriteStoreRepository; // 添加这个
    private final OrderWebSocketHandler orderWebSocketHandler;
    private final ObjectMapper objectMapper;
    private final PointTransactionRepository pointTransactionRepository;

    // Constants
    private static final BigDecimal DEFAULT_PACKAGE_FEE = BigDecimal.valueOf(1.00);
    private static final BigDecimal DEFAULT_DELIVERY_FEE = BigDecimal.valueOf(3.00);
    private static final int ORDER_PAYMENT_EXPIRATION_MINUTES = 15;
    private static final int ORDER_CANCEL_DEADLINE_MINUTES = 10;
    private static final int ORDER_REFUND_DEADLINE_DAYS = 7;
    private static final int ORDER_RATE_DEADLINE_DAYS = 14;

    @Override
    @Transactional(readOnly = true)
    public CheckoutResDTO getCheckoutDetails(Long userId, CheckoutReqDTO reqDTO) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User not found."));

        List<CartItemEntity> cartItems = cartItemRepository.findAllById(reqDTO.getItemIds());
        if (cartItems.isEmpty() || cartItems.stream().anyMatch(item -> !item.getUser().getId().equals(userId))) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND, "Selected cart items not found or do not belong to user.");
        }

        List<CheckoutResDTO.OrderItemSummaryDTO> orderItemSummaries = new ArrayList<>();
        BigDecimal productAmount = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal packageFee = DEFAULT_PACKAGE_FEE.multiply(BigDecimal.valueOf(cartItems.size()));

        for (CartItemEntity cartItem : cartItems) {
            ProductEntity product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + cartItem.getProduct().getId()));

            if (!product.getIsActive()) {
                throw new BusinessException(ErrorCode.PRODUCT_OFFLINE, "Product " + product.getName() + " is offline.");
            }
            if (product.getStock() < cartItem.getQuantity()) {
                throw new BusinessException(ErrorCode.PRODUCT_STOCK_INSUFFICIENT, "Product " + product.getName() + " stock insufficient.");
            }

            BigDecimal itemBasePrice = product.getPrice();
            BigDecimal itemSubtotal = itemBasePrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            CheckoutResDTO.CustomizationsDTO customizationsDTO = new CheckoutResDTO.CustomizationsDTO();
            List<String> toppingLabels = new ArrayList<>();

            for (CartItemCustomizationEntity customization : cartItem.getCustomizations()) {
                BigDecimal adjustment = customizationOptionRepository.findByCustomizationTypeTypeNameAndValue(
                                customization.getCustomizationTypeName(), customization.getOptionValue())
                        .map(ProductCustomizationOptionEntity::getPriceAdjustment)
                        .orElse(customization.getPriceAdjustmentAtAdd());

                itemSubtotal = itemSubtotal.add(adjustment.multiply(BigDecimal.valueOf(customization.getQuantity())));

                switch (customization.getCustomizationTypeName()) {
                    case "sweetness" -> customizationsDTO.setSweetness(customization.getOptionLabel());
                    case "temperature" -> customizationsDTO.setTemperature(customization.getOptionLabel());
                    case "toppings" -> {
                        for (int i = 0; i < customization.getQuantity(); i++) {
                            toppingLabels.add(customization.getOptionLabel());
                        }
                    }
                }
            }
            customizationsDTO.setToppings(toppingLabels);

            orderItemSummaries.add(new CheckoutResDTO.OrderItemSummaryDTO(
                    product.getId(),
                    product.getName(),
                    product.getMainImageUrl(),
                    cartItem.getQuantity(),
                    product.getPrice(),
                    customizationsDTO,
                    itemSubtotal
            ));
            productAmount = productAmount.add(itemSubtotal);
        }

        UserAddressResDTO.AddressDTO addressDTO = null;
        if (reqDTO.getAddressId() != null) {
            UserAddressEntity addressEntity = userAddressRepository.findById(reqDTO.getAddressId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND, "Delivery address not found."));
            addressDTO = mapToAddressDTO(addressEntity);
        } else {
            Optional<UserAddressEntity> defaultAddress = userAddressRepository.findByUserIdAndIsDefaultTrue(userId);
            if (defaultAddress.isPresent()) {
                addressDTO = mapToAddressDTO(defaultAddress.get());
            }
        }

        StoreEntity store = null;
        HomePageResDTO.NearbyStoreDTO storeDTO = null;
        BigDecimal deliveryFee = BigDecimal.ZERO;

        if (addressDTO != null && addressDTO.getLatitude() != null && addressDTO.getLongitude() != null) {
            List<StoreEntity> nearbyStores = storeRepository.findNearbyStoresNative(
                    addressDTO.getLatitude(), addressDTO.getLongitude(), 5000.0, 1);
            if (!nearbyStores.isEmpty()) {
                store = nearbyStores.get(0);
                storeDTO = mapToNearbyStoreDTO(store, addressDTO.getLatitude(), addressDTO.getLongitude(), userId);
                deliveryFee = store.getDeliveryFee();
            }
        }
        if (store == null) {
            store = storeRepository.findByIsActiveTrue().stream().findFirst().orElse(null);
            if (store != null) {
                storeDTO = mapToNearbyStoreDTO(store, BigDecimal.ZERO, BigDecimal.ZERO, userId);
                deliveryFee = store.getDeliveryFee();
            } else {
                throw new BusinessException(ErrorCode.STORE_NOT_FOUND, "No active stores available.");
            }
        }

        CouponListResDTO.CouponDTO selectedCouponDTO = null;
        if (reqDTO.getCouponId() != null) {
            UserCouponEntity userCoupon = userCouponRepository.findById(reqDTO.getCouponId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND, "Coupon not found."));
            if (!userCoupon.getUser().getId().equals(userId) || !"available".equals(userCoupon.getStatus()) || userCoupon.getExpireAt().isBefore(Instant.now())) {
                throw new BusinessException(ErrorCode.COUPON_NOT_APPLICABLE, "Selected coupon is not available or expired.");
            }
            selectedCouponDTO = mapToCouponDTO(userCoupon);

            BigDecimal discountValue = calculateCouponDiscount(userCoupon.getCouponTemplate(), productAmount);
            totalDiscount = totalDiscount.add(discountValue);
        }

        List<CouponListResDTO.CouponDTO> availableCoupons = userCouponRepository.findByUserIdAndStatusOrderByExpireAtAsc(userId, "available")
                .stream()
                .filter(uc -> uc.getExpireAt().isAfter(Instant.now()))
                .map(this::mapToCouponDTO)
                .collect(Collectors.toList());

        Integer availablePoints = user.getPoints();
        BigDecimal balance = user.getBalance();
        Integer pointsRate = 100;

        BigDecimal finalAmount = productAmount.add(packageFee).add(deliveryFee).subtract(totalDiscount);
        finalAmount = finalAmount.max(BigDecimal.ZERO);

        CheckoutResDTO.OrderSummaryDTO summary = new CheckoutResDTO.OrderSummaryDTO(
                productAmount,
                deliveryFee,
                packageFee,
                totalDiscount,
                finalAmount,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        CheckoutResDTO.DeliveryTimeDTO deliveryTime = new CheckoutResDTO.DeliveryTimeDTO(
                LocalDate.now(), "14:30-15:00", true
        );

        List<CheckoutResDTO.WarningDTO> warnings = new ArrayList<>();

        return new CheckoutResDTO(
                orderItemSummaries,
                addressDTO,
                availableCoupons,
                selectedCouponDTO,
                summary,
                availablePoints,
                pointsRate,
                balance,
                deliveryTime,
                storeDTO,
                store != null ? store.getMinimumOrderAmount() : BigDecimal.ZERO,
                reqDTO.getRemark(),
                warnings
        );
    }

    @Override
    @Transactional(readOnly = true)
    public OrderValidateResDTO validateOrder(Long userId, OrderValidateReqDTO reqDTO) {
        OrderValidateResDTO resDTO = new OrderValidateResDTO();
        resDTO.setIsValid(true);
        resDTO.setInvalidItems(new ArrayList<>());
        resDTO.setWarnings(new ArrayList<>());

        log.info("Performing mock order validation for user {}", userId);

        List<Long> productIdsToValidate = new ArrayList<>();
        if (reqDTO.getItemIds() != null && !reqDTO.getItemIds().isEmpty()) {
            for (Long itemId : reqDTO.getItemIds()) {
                Optional<CartItemEntity> cartItemOpt = cartItemRepository.findById(itemId);
                if (cartItemOpt.isEmpty() || !cartItemOpt.get().getUser().getId().equals(userId)) {
                    resDTO.getInvalidItems().add(new OrderValidateResDTO.InvalidItemDTO(itemId, "cart_item_not_found", "购物车商品不存在"));
                    resDTO.setIsValid(false);
                    continue;
                }
                productIdsToValidate.add(cartItemOpt.get().getProduct().getId());
            }
        }

        for (Long productId : productIdsToValidate) {
            ProductEntity product = productRepository.findById(productId).orElse(null);

            if (product == null || !product.getIsActive()) {
                resDTO.getInvalidItems().add(new OrderValidateResDTO.InvalidItemDTO(productId, "product_offline", "商品已下架"));
                resDTO.setIsValid(false);
            } else if (product.getStock() <= 0) {
                resDTO.getInvalidItems().add(new OrderValidateResDTO.InvalidItemDTO(productId, "stock_out", "库存不足"));
                resDTO.setIsValid(false);
            }
        }

        if (reqDTO.getStoreId() != null) {
            Optional<StoreEntity> storeOpt = storeRepository.findById(reqDTO.getStoreId());
            if (storeOpt.isPresent() && "closed".equals(storeOpt.get().getStatus())) {
                resDTO.getWarnings().add(new OrderValidateResDTO.WarningDTO("store_closed", "您选择的门店已打烊"));
                resDTO.setIsValid(false);
            }
        }

        if (!resDTO.getInvalidItems().isEmpty()) {
            resDTO.setIsValid(false);
        }

        return resDTO;
    }

    @Override
    @Transactional
    public OrderCreateResDTO createOrder(Long userId, OrderCreateReqDTO reqDTO) {
        return processOrderCreation(userId, reqDTO, false);
    }

    @Override
    @Transactional
    public OrderCreateResDTO buyNow(Long userId, OrderCreateReqDTO reqDTO) {
        if (reqDTO.getItems() == null || reqDTO.getItems().size() != 1) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "Buy now only supports a single product.");
        }
        return processOrderCreation(userId, reqDTO, true);
    }

    private OrderCreateResDTO processOrderCreation(Long userId, OrderCreateReqDTO reqDTO, boolean isBuyNow) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User not found."));

        OrderValidateResDTO validationResult = validateOrder(userId, mapToOrderValidateReqDTO(reqDTO));
        if (!validationResult.getIsValid()) {
            throw new BusinessException(ErrorCode.CHECKOUT_VALIDATION_FAILED, "Order validation failed.");
        }

        OrderEntity order = new OrderEntity();
        order.setUser(user);
        order.setOrderNo(generateOrderNo());
        order.setType(reqDTO.getType());
        order.setStatus(OrderStatus.CREATED.getCode());
        order.setStatusText(OrderStatus.CREATED.getDescription());
        order.setRemark(reqDTO.getRemark());
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        order.setIsRated(false);
        order.setPointsUsed(reqDTO.getPoints() != null ? reqDTO.getPoints() : 0);
        order.setBalanceUsed(reqDTO.getBalance() != null ? reqDTO.getBalance() : BigDecimal.ZERO);

        if ("delivery".equalsIgnoreCase(reqDTO.getType())) {
            UserAddressEntity address = userAddressRepository.findById(reqDTO.getAddressId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND, "Delivery address not found."));
            order.setDeliveryAddress(address);
            order.setDeliveryTimeExpected(reqDTO.getDeliveryTime());
        } else if ("pickup".equalsIgnoreCase(reqDTO.getType())) {
            StoreEntity store = storeRepository.findById(reqDTO.getStoreId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND, "Pickup store not found."));
            order.setPickupStore(store);
            order.setEstimatedReadyTime(reqDTO.getDeliveryTime());
            order.setPickupCode(generatePickupCode());
        } else {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "Invalid order type.");
        }

        if (reqDTO.getInvoice() != null) {
            order.setInvoiceType(reqDTO.getInvoice().getType());
            order.setInvoiceTitle(reqDTO.getInvoice().getTitle());
            order.setInvoiceTaxNumber(reqDTO.getInvoice().getTaxNumber());
        }

        BigDecimal productAmount = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal packageFee = DEFAULT_PACKAGE_FEE.multiply(BigDecimal.valueOf(reqDTO.getItems().size()));
        BigDecimal deliveryFee = BigDecimal.ZERO;

        if (order.getDeliveryAddress() != null && order.getPickupStore() != null) {
            deliveryFee = order.getPickupStore().getDeliveryFee();
        } else if ("delivery".equalsIgnoreCase(reqDTO.getType())) {
            deliveryFee = DEFAULT_DELIVERY_FEE;
        }

        List<OrderItemEntity> orderItems = new ArrayList<>();
        for (OrderCreateReqDTO.OrderItemCreateDTO itemDTO : reqDTO.getItems()) {
            ProductEntity product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + itemDTO.getProductId()));

            if (product.getStock() < itemDTO.getQuantity()) {
                throw new BusinessException(ErrorCode.PRODUCT_STOCK_INSUFFICIENT, "Product " + product.getName() + " stock insufficient.");
            }
            product.setStock(product.getStock() - itemDTO.getQuantity());
            productRepository.save(product);

            BigDecimal itemBasePrice = product.getPrice();
            BigDecimal itemSubtotal = itemBasePrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));

            OrderItemEntity orderItem = new OrderItemEntity();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setProductImageUrl(product.getMainImageUrl());
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPriceAtOrder(product.getPrice());
            orderItem.setOriginalPriceAtOrder(product.getOriginalPrice());
            orderItem.setCreatedAt(Instant.now());

            List<OrderItemCustomizationEntity> itemCustomizations = new ArrayList<>();
            if (itemDTO.getCustomizations() != null) {
                Map<String, ProductCustomizationTypeEntity> productCustomizationTypes = customizationTypeRepository.findByProductIdAndIsEnabledTrueOrderBySortOrderAsc(product.getId())
                        .stream()
                        .collect(Collectors.toMap(ProductCustomizationTypeEntity::getTypeName, type -> type));

                if (itemDTO.getCustomizations().getSweetness() != null) {
                    ProductCustomizationTypeEntity type = productCustomizationTypes.get("sweetness");
                    if (type != null) {
                        ProductCustomizationOptionEntity option = type.getOptions().stream()
                                .filter(o -> o.getValue().equals(itemDTO.getCustomizations().getSweetness()))
                                .findFirst().orElse(null);
                        if (option != null) {
                            itemSubtotal = itemSubtotal.add(option.getPriceAdjustment());
                            itemCustomizations.add(createOrderItemCustomization(orderItem, type, option, 1));
                        }
                    }
                }
                if (itemDTO.getCustomizations().getTemperature() != null) {
                    ProductCustomizationTypeEntity type = productCustomizationTypes.get("temperature");
                    if (type != null) {
                        ProductCustomizationOptionEntity option = type.getOptions().stream()
                                .filter(o -> o.getValue().equals(itemDTO.getCustomizations().getTemperature()))
                                .findFirst().orElse(null);
                        if (option != null) {
                            itemSubtotal = itemSubtotal.add(option.getPriceAdjustment());
                            itemCustomizations.add(createOrderItemCustomization(orderItem, type, option, 1));
                        }
                    }
                }
                if (itemDTO.getCustomizations().getToppings() != null) {
                    ProductCustomizationTypeEntity type = productCustomizationTypes.get("toppings");
                    if (type != null) {
                        Map<String, ProductCustomizationOptionEntity> toppingOptions = type.getOptions().stream()
                                .collect(Collectors.toMap(ProductCustomizationOptionEntity::getValue, o -> o));
                        for (OrderCreateReqDTO.ToppingItem toppingItem : itemDTO.getCustomizations().getToppings()) {
                            ProductCustomizationOptionEntity option = toppingOptions.get(toppingItem.getId());
                            if (option == null) {
                                throw new BusinessException(ErrorCode.INVALID_PARAM, "Invalid topping option: " + toppingItem.getId());
                            }
                            if (option.getStock() != null && option.getStock() < toppingItem.getQuantity()) {
                                throw new BusinessException(ErrorCode.PRODUCT_STOCK_INSUFFICIENT, "Topping " + option.getLabel() + " stock insufficient.");
                            }
                            itemSubtotal = itemSubtotal.add(option.getPriceAdjustment().multiply(BigDecimal.valueOf(toppingItem.getQuantity())));
                            itemCustomizations.add(createOrderItemCustomization(orderItem, type, option, toppingItem.getQuantity()));
                            if (option.getStock() != null) {
                                option.setStock(option.getStock() - toppingItem.getQuantity());
                                customizationOptionRepository.save(option);
                            }
                        }
                    }
                }
            }
            orderItem.setSubtotal(itemSubtotal);
            orderItem.setCustomizations(itemCustomizations);
            orderItems.add(orderItem);
            productAmount = productAmount.add(itemSubtotal);
        }
        order.setItems(orderItems);
        order.setProductAmount(productAmount);
        order.setPackageFee(packageFee);
        order.setDeliveryFee(deliveryFee);

        if (reqDTO.getCouponId() != null) {
            UserCouponEntity userCoupon = userCouponRepository.findById(reqDTO.getCouponId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND, "Coupon not found."));
            if (!userCoupon.getUser().getId().equals(userId) || !"available".equals(userCoupon.getStatus()) || userCoupon.getExpireAt().isBefore(Instant.now())) {
                throw new BusinessException(ErrorCode.COUPON_NOT_APPLICABLE, "Selected coupon is not available or expired.");
            }
            BigDecimal discountValue = calculateCouponDiscount(userCoupon.getCouponTemplate(), productAmount);
            order.setDiscountAmount(order.getDiscountAmount().add(discountValue));
            order.setCouponId(userCoupon.getId());
            userCoupon.setStatus("used");
            userCoupon.setUsedAt(Instant.now());
            userCouponRepository.save(userCoupon);
        }

        BigDecimal pointsDiscountAmount = BigDecimal.ZERO;
        if (reqDTO.getPoints() != null && reqDTO.getPoints() > 0) {
            if (user.getPoints() < reqDTO.getPoints()) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_POINTS, "Insufficient points.");
            }
            pointsDiscountAmount = BigDecimal.valueOf(reqDTO.getPoints()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            order.setPointsDiscountAmount(pointsDiscountAmount);
            user.setPoints(user.getPoints() - reqDTO.getPoints());
            PointTransactionEntity pointTransaction = new PointTransactionEntity();
            pointTransaction.setUser(user);
            pointTransaction.setPointsChange(-reqDTO.getPoints());
            pointTransaction.setBalanceAfterTransaction(user.getPoints());
            pointTransaction.setType("use");
            pointTransaction.setDescription("使用积分抵扣订单");
            pointTransaction.setRelatedType("order");
            pointTransaction.setRelatedId(order.getOrderNo());
            pointTransaction.setCreatedAt(Instant.now());
            pointTransactionRepository.save(pointTransaction);
        }

        BigDecimal balanceDiscountAmount = BigDecimal.ZERO;
        if (reqDTO.getBalance() != null && reqDTO.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            if (user.getBalance().compareTo(reqDTO.getBalance()) < 0) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_POINTS, "Insufficient balance.");
            }
            balanceDiscountAmount = reqDTO.getBalance();
            order.setBalanceDiscountAmount(balanceDiscountAmount);
            user.setBalance(user.getBalance().subtract(reqDTO.getBalance()));
        }
        userRepository.save(user);

        BigDecimal totalAmountBeforePayment = productAmount.add(packageFee).add(deliveryFee)
                .subtract(order.getDiscountAmount())
                .subtract(order.getPointsDiscountAmount())
                .subtract(order.getBalanceDiscountAmount());

        order.setPayAmount(totalAmountBeforePayment.max(BigDecimal.ZERO));
        order.setTotalAmount(totalAmountBeforePayment.max(BigDecimal.ZERO));

        OrderEntity savedOrder = orderRepository.save(order);

        if (reqDTO.getCouponId() != null) {
            UserCouponEntity userCoupon = userCouponRepository.findById(reqDTO.getCouponId())
                    .orElse(null);
            if (userCoupon != null) {
                userCoupon.setOrderId(savedOrder.getId());
                userCouponRepository.save(userCoupon);
            }
        }

        for (OrderItemEntity orderItem : orderItems) {
            orderItem.setOrder(savedOrder);
            OrderItemEntity savedOrderItem = orderItemRepository.save(orderItem);
            orderItem.getCustomizations().forEach(cust -> cust.setOrderItem(savedOrderItem));
            orderItemCustomizationRepository.saveAll(orderItem.getCustomizations());
        }

        addOrderStatusTimeline(savedOrder, OrderStatus.CREATED, true);

        savedOrder.setCancelDeadline(Instant.now().plusSeconds(ORDER_CANCEL_DEADLINE_MINUTES * 60L));
        orderRepository.save(savedOrder);

        boolean needPay = savedOrder.getPayAmount().compareTo(BigDecimal.ZERO) > 0;
        OrderCreateResDTO.PayInfoDTO payInfo = null;
        if (needPay) {
            log.info("Initiating payment for order {}", savedOrder.getOrderNo());
            payInfo = new OrderCreateResDTO.PayInfoDTO(
                    "MOCK_PAY_" + UUID.randomUUID().toString(),
                    "wechat",
                    savedOrder.getPayAmount(),
                    Instant.now().plusSeconds(ORDER_PAYMENT_EXPIRATION_MINUTES * 60L)
            );
            PaymentEntity payment = new PaymentEntity();
            payment.setOrder(savedOrder);
            payment.setPayId(payInfo.getPayId());
            payment.setPayType(payInfo.getPayType());
            payment.setPayAmount(payInfo.getPayAmount());
            payment.setPayStatus(PayStatus.UNPAID.getCode());
            payment.setExpireTime(payInfo.getExpireTime());
            payment.setIsSandbox(true);
            payment.setCreatedAt(Instant.now());
            payment.setUpdatedAt(Instant.now());
            paymentRepository.save(payment);
        }

        if (!isBuyNow) {
            log.warn("Cart clearing logic is simplified. Needs actual cart item IDs for clearance.");
        }

        publishOrderStatusUpdate(savedOrder.getId(), savedOrder.getStatus(), savedOrder.getStatusText(), savedOrder.getEstimatedReadyTime());

        return new OrderCreateResDTO(
                savedOrder.getId(),
                savedOrder.getOrderNo(),
                savedOrder.getTotalAmount(),
                savedOrder.getPayAmount(),
                savedOrder.getPointsUsed(),
                savedOrder.getBalanceUsed(),
                order.getDiscountAmount(),
                needPay,
                payInfo
        );
    }

    @Override
    @Transactional
    public PaymentResDTO initiatePayment(Long userId, Long orderId, PaymentReqDTO reqDTO) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found."));

        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Access denied to order.");
        }
        if (!OrderStatus.CREATED.getCode().equals(order.getStatus()) && !PayStatus.UNPAID.getCode().equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID, "Order is not in a payable state.");
        }

        Optional<PaymentEntity> existingPaymentOptional = paymentRepository.findByOrderId(orderId);
        PaymentEntity payment;

        if (existingPaymentOptional.isPresent() && PayStatus.UNPAID.getCode().equals(existingPaymentOptional.get().getPayStatus()) && existingPaymentOptional.get().getExpireTime().isAfter(Instant.now())) {
            payment = existingPaymentOptional.get();
        } else {
            payment = existingPaymentOptional.orElseGet(PaymentEntity::new);
            payment.setOrder(order);
            payment.setPayId("MOCK_PAY_" + UUID.randomUUID().toString());
            payment.setPayAmount(order.getPayAmount());
            payment.setPayStatus(PayStatus.UNPAID.getCode());
            payment.setExpireTime(Instant.now().plusSeconds(ORDER_PAYMENT_EXPIRATION_MINUTES * 60L));
            payment.setCreatedAt(Instant.now());
        }

        payment.setPayType(reqDTO.getPayType());
        payment.setChannel(reqDTO.getChannel());
        payment.setUpdatedAt(Instant.now());
        payment = paymentRepository.save(payment);

        Map<String, String> payParams = new HashMap<>();
        payParams.put("tradeNO", payment.getPayId());
        payParams.put("totalAmount", payment.getPayAmount().toPlainString());
        payParams.put("subject", "奶茶小屋订单: " + order.getOrderNo());
        payParams.put("body", "商品详情...");
        payParams.put("timeoutExpress", ORDER_PAYMENT_EXPIRATION_MINUTES + "m");

        String paymentUrl = null;
        if ("h5".equals(reqDTO.getChannel())) {
            paymentUrl = "https://mock-payment-gateway.com/pay?id=" + payment.getPayId();
        }

        return new PaymentResDTO(
                payment.getPayId(),
                order.getOrderNo(),
                payment.getPayAmount(),
                payment.getPayType(),
                payParams,
                true,
                paymentUrl
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentStatusResDTO getPaymentStatus(Long userId, Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found."));

        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Access denied to order.");
        }

        PaymentEntity payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Payment record not found for this order."));

        return new PaymentStatusResDTO(
                order.getId(),
                payment.getPayStatus(),
                payment.getPayTime(),
                payment.getPayAmount(),
                payment.getTransactionId()
        );
    }

    @Override
    @Transactional
    public void cancelPayment(Long userId, Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found."));

        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Access denied to order.");
        }

        PaymentEntity payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Payment record not found for this order."));

        if (!PayStatus.UNPAID.getCode().equals(payment.getPayStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID, "Payment is not in an unpaid state and cannot be cancelled.");
        }

        log.info("Cancelling pending payment {} for order {}", payment.getPayId(), order.getOrderNo());

        payment.setPayStatus(PayStatus.CANCELLED.getCode());
        payment.setUpdatedAt(Instant.now());
        paymentRepository.save(payment);

        log.info("Payment {} for order {} has been cancelled.", payment.getPayId(), order.getOrderNo());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderListResDTO getUserOrders(Long userId, String status, String type, Instant startDate, Instant endDate, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User not found."));

        Specification<OrderEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            if (status != null && !status.equalsIgnoreCase("all")) {
                predicates.add(cb.equal(root.get("status"), status.toLowerCase()));
            }
            if (type != null && !type.equalsIgnoreCase("all")) {
                predicates.add(cb.equal(root.get("type"), type.toLowerCase()));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<OrderEntity> orderPage = orderRepository.findAll(spec, pageable);

        List<OrderListResDTO.OrderSummaryDTO> orderSummaryDTOs = orderPage.getContent().stream()
                .map(this::mapToOrderSummaryDTO)
                .collect(Collectors.toList());

        OrderListResDTO.OrderStatsDTO stats = new OrderListResDTO.OrderStatsDTO();
        stats.setAll((int) orderRepository.countByUserId(userId));
        stats.setPending((int) orderRepository.countByUserIdAndStatus(userId, OrderStatus.CREATED.getCode()));
        stats.setPaid((int) orderRepository.countByUserIdAndStatus(userId, OrderStatus.PAID.getCode()));
        stats.setMaking((int) orderRepository.countByUserIdAndStatus(userId, OrderStatus.MAKING.getCode()));
        stats.setReady((int) orderRepository.countByUserIdAndStatus(userId, OrderStatus.READY.getCode()));
        stats.setCompleted((int) orderRepository.countByUserIdAndStatus(userId, OrderStatus.COMPLETED.getCode()));
        stats.setCancelled((int) orderRepository.countByUserIdAndStatus(userId, OrderStatus.CANCELLED.getCode()));

        OrderListResDTO resDTO = new OrderListResDTO();
        resDTO.setOrders(orderSummaryDTOs);
        resDTO.setTotal((int) orderPage.getTotalElements());
        resDTO.setPage(pageable.getPageNumber() + 1);
        resDTO.setLimit(pageable.getPageSize());
        resDTO.setStats(stats);
        return resDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResDTO getOrderDetail(Long userId, Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found."));

        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Access denied to order.");
        }

        return mapToOrderDetailResDTO(order, userId);
    }

    @Override
    @Transactional
    public void cancelOrder(Long userId, Long orderId, OrderCancelReqDTO reqDTO) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found."));

        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Access denied to order.");
        }

        if (!OrderStatus.CREATED.getCode().equals(order.getStatus()) && !OrderStatus.PAID.getCode().equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_CANCEL, "Order cannot be cancelled in its current state.");
        }
        if (order.getCancelDeadline() != null && Instant.now().isAfter(order.getCancelDeadline())) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_CANCEL, "Cancellation deadline has passed.");
        }

        if (OrderStatus.PAID.getCode().equals(order.getStatus())) {
            log.info("Initiating refund for cancelled order {} for amount {}", order.getOrderNo(), order.getPayAmount());
            PaymentEntity payment = paymentRepository.findByOrderId(order.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.SYSTEM_ERROR, "Payment record not found for paid order."));
            payment.setPayStatus(PayStatus.CANCELLED.getCode());
            paymentRepository.save(payment);
            if (order.getPointsUsed() > 0) {
                UserEntity user = order.getUser();
                user.setPoints(user.getPoints() + order.getPointsUsed());
                userRepository.save(user);
                PointTransactionEntity pointTransaction = new PointTransactionEntity();
                pointTransaction.setUser(user);
                pointTransaction.setPointsChange(order.getPointsUsed());
                pointTransaction.setBalanceAfterTransaction(user.getPoints());
                pointTransaction.setType("earn");
                pointTransaction.setDescription("取消订单返还积分");
                pointTransaction.setRelatedType("order");
                pointTransaction.setRelatedId(order.getOrderNo());
                pointTransaction.setCreatedAt(Instant.now());
                pointTransactionRepository.save(pointTransaction);
            }
            if (order.getBalanceUsed().compareTo(BigDecimal.ZERO) > 0) {
                UserEntity user = order.getUser();
                user.setBalance(user.getBalance().add(order.getBalanceUsed()));
                userRepository.save(user);
            }
            if (order.getCouponId() != null) {
                UserCouponEntity userCoupon = userCouponRepository.findById(order.getCouponId()).orElse(null);
                if (userCoupon != null) {
                    userCoupon.setStatus("available");
                    userCoupon.setUsedAt(null);
                    userCoupon.setOrderId(null);
                    userCouponRepository.save(userCoupon);
                }
            }
        }

        for (OrderItemEntity item : order.getItems()) {
            ProductEntity product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
            for (OrderItemCustomizationEntity customization : item.getCustomizations()) {
                if ("toppings".equalsIgnoreCase(customization.getCustomizationTypeName())) {
                    Optional<ProductCustomizationTypeEntity> typeOpt = customizationTypeRepository.findByTypeNameAndProductId(customization.getCustomizationTypeName(), product.getId());
                    typeOpt.ifPresent(typeEntity -> {
                        Optional<ProductCustomizationOptionEntity> optionOpt = typeEntity.getOptions().stream()
                                .filter(o -> o.getValue().equals(customization.getOptionValue()))
                                .findFirst();
                        optionOpt.ifPresent(option -> {
                            if (option.getStock() != null) {
                                option.setStock(option.getStock() + customization.getQuantity());
                                customizationOptionRepository.save(option);
                            }
                        });
                    });
                }
            }
        }

        order.setStatus(OrderStatus.CANCELLED.getCode());
        order.setStatusText(OrderStatus.CANCELLED.getDescription());
        orderRepository.save(order);
        addOrderStatusTimeline(order, OrderStatus.CANCELLED, true);

        publishOrderStatusUpdate(order.getId(), order.getStatus(), order.getStatusText(), null);
    }

    @Override
    @Transactional
    public void confirmOrder(Long userId, Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found."));

        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Access denied to order.");
        }

        if (!OrderStatus.READY.getCode().equals(order.getStatus()) && !OrderStatus.DELIVERING.getCode().equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID, "Order cannot be confirmed in its current state.");
        }

        order.setStatus(OrderStatus.COMPLETED.getCode());
        order.setStatusText(OrderStatus.COMPLETED.getDescription());
        order.setPickupTimeActual(Instant.now());
        orderRepository.save(order);
        addOrderStatusTimeline(order, OrderStatus.COMPLETED, true);

        order.setRefundDeadline(Instant.now().plusSeconds(ORDER_REFUND_DEADLINE_DAYS * 24 * 60 * 60L));
        order.setRateDeadline(Instant.now().plusSeconds(ORDER_RATE_DEADLINE_DAYS * 24 * 60 * 60L));
        orderRepository.save(order);

        publishOrderStatusUpdate(order.getId(), order.getStatus(), order.getStatusText(), null);

        UserEntity user = order.getUser();
        int pointsEarned = order.getTotalAmount().multiply(BigDecimal.TEN).intValue();
        user.setPoints(user.getPoints() + pointsEarned);
        userRepository.save(user);
        PointTransactionEntity pointTransaction = new PointTransactionEntity();
        pointTransaction.setUser(user);
        pointTransaction.setPointsChange(pointsEarned);
        pointTransaction.setBalanceAfterTransaction(user.getPoints());
        pointTransaction.setType("earn");
        pointTransaction.setDescription("完成订单获得积分");
        pointTransaction.setRelatedType("order");
        pointTransaction.setRelatedId(order.getOrderNo());
        pointTransaction.setCreatedAt(Instant.now());
        pointTransactionRepository.save(pointTransaction);
    }

    @Override
    @Transactional
    public void remindOrder(Long userId, Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found."));

        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Access denied to order.");
        }

        if (!OrderStatus.PAID.getCode().equals(order.getStatus()) && !OrderStatus.MAKING.getCode().equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID, "Order cannot be reminded in its current state.");
        }

        log.info("User {} reminded for order {}. Current status: {}", userId, orderId, order.getStatus());
    }

    @Override
    @Transactional
    public void applyOrderRefund(Long userId, Long orderId, OrderRefundApplyReqDTO reqDTO) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found."));

        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Access denied to order.");
        }

        if (!OrderStatus.COMPLETED.getCode().equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_REFUND, "Refund can only be applied for completed orders.");
        }
        if (order.getRefundDeadline() != null && Instant.now().isAfter(order.getRefundDeadline())) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_REFUND, "Refund deadline has passed.");
        }
        if ("refunded".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_REFUND, "Order has already been refunded.");
        }

        OrderRefundEntity refund = new OrderRefundEntity();
        refund.setOrder(order);
        refund.setUser(order.getUser());
        refund.setReason(reqDTO.getReason());
        refund.setDescription(reqDTO.getDescription());
        refund.setStatus("pending");
        refund.setRefundAmount(order.getPayAmount());
        refund.setCreatedAt(Instant.now());
        refund.setUpdatedAt(Instant.now());
        refund = orderRefundRepository.save(refund);

        if (reqDTO.getImages() != null && !reqDTO.getImages().isEmpty()) {
            for (String imageUrl : reqDTO.getImages()) {
                OrderRefundImageEntity refundImage = new OrderRefundImageEntity();
                refundImage.setRefund(refund);
                refundImage.setImageUrl(imageUrl);
                refundImage.setCreatedAt(Instant.now());
                orderRefundImageRepository.save(refundImage);
            }
        }

        order.setStatus("refund_pending");
        order.setStatusText("退款申请中");
        orderRepository.save(order);
        addOrderStatusTimeline(order, OrderStatus.REFUNDED, false);

        publishOrderStatusUpdate(order.getId(), "refund_pending", "退款申请中", null);
    }

    @Override
    @Transactional
    public void addOrderReview(Long userId, Long orderId, OrderReviewReqDTO reqDTO) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found."));

        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Access denied to order.");
        }
        if (!OrderStatus.COMPLETED.getCode().equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID, "Only completed orders can be reviewed.");
        }
        if (order.getIsRated()) {
            throw new BusinessException(ErrorCode.CONFLICT, "Order has already been rated.");
        }
        if (order.getRateDeadline() != null && Instant.now().isAfter(order.getRateDeadline())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Review deadline has passed.");
        }

        OrderReviewEntity review = new OrderReviewEntity();
        review.setOrder(order);
        review.setUser(order.getUser());
        review.setRating(reqDTO.getRating().shortValue());
        review.setContent(reqDTO.getContent());
        review.setIsAnonymous(reqDTO.getAnonymous() != null ? reqDTO.getAnonymous() : false);
        review.setCreatedAt(Instant.now());
        review.setUpdatedAt(Instant.now());
        review = orderReviewRepository.save(review);

        if (reqDTO.getImages() != null && !reqDTO.getImages().isEmpty()) {
            for (String imageUrl : reqDTO.getImages()) {
                OrderReviewImageEntity reviewImage = new OrderReviewImageEntity();
                reviewImage.setReview(review);
                reviewImage.setImageUrl(imageUrl);
                reviewImage.setCreatedAt(Instant.now());
                orderReviewImageRepository.save(reviewImage);
            }
        }

        if (reqDTO.getTags() != null && !reqDTO.getTags().isEmpty()) {
            List<ReviewTagEntity> tags = reqDTO.getTags().stream()
                    .map(tagName -> {
                        Optional<ReviewTagEntity> existingTag = reviewTagRepository.findByName(tagName);
                        return existingTag.orElseGet(() -> {
                            ReviewTagEntity newTag = new ReviewTagEntity();
                            newTag.setName(tagName);
                            return reviewTagRepository.save(newTag);
                        });
                    })
                    .collect(Collectors.toList());
            review.setTags(tags);
            orderReviewRepository.save(review);
        }

        order.setIsRated(true);
        orderRepository.save(order);

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItemEntity orderItem : order.getItems()) {
                ProductEntity product = orderItem.getProduct();
                product.setRatingCount(product.getRatingCount() + 1);
                BigDecimal newTotalRatingSum = product.getRating().multiply(BigDecimal.valueOf(product.getRatingCount() - 1))
                        .add(BigDecimal.valueOf(reqDTO.getRating()));
                product.setRating(newTotalRatingSum.divide(BigDecimal.valueOf(product.getRatingCount()), 1, RoundingMode.HALF_UP));
                productRepository.save(product);
            }
        }
    }

    @Override
    public void publishOrderStatusUpdate(Long orderId, String newStatus, String statusText, Instant estimatedTime) {
        OrderStatusChangedWsDTO.OrderStatusChangedData data = new OrderStatusChangedWsDTO.OrderStatusChangedData(
                orderId,
                orderRepository.findById(orderId).map(OrderEntity::getOrderNo).orElse("N/A"),
                null,
                newStatus,
                statusText,
                System.currentTimeMillis(),
                estimatedTime
        );
        OrderStatusChangedWsDTO wsDTO = new OrderStatusChangedWsDTO("order_status_changed", data);
        orderWebSocketHandler.sendOrderStatusUpdate(orderId, wsDTO);
        log.info("Published order status update for order {}: {}", orderId, newStatus);
    }

    private OrderItemCustomizationEntity createOrderItemCustomization(
            OrderItemEntity orderItem,
            ProductCustomizationTypeEntity type,
            ProductCustomizationOptionEntity option,
            Integer quantity) {
        OrderItemCustomizationEntity customization = new OrderItemCustomizationEntity();
        customization.setOrderItem(orderItem);
        customization.setCustomizationTypeName(type.getTypeName());
        customization.setOptionValue(option.getValue());
        customization.setOptionLabel(option.getLabel());
        customization.setPriceAdjustmentAtOrder(option.getPriceAdjustment());
        customization.setQuantity(quantity);
        customization.setCreatedAt(Instant.now());
        return customization;
    }

    private OrderValidateReqDTO mapToOrderValidateReqDTO(OrderCreateReqDTO reqDTO) {
        OrderValidateReqDTO validateReq = new OrderValidateReqDTO();
        validateReq.setAddressId(reqDTO.getAddressId());
        validateReq.setStoreId(reqDTO.getStoreId());
        validateReq.setCouponId(reqDTO.getCouponId());
        validateReq.setRemark(reqDTO.getRemark());
        validateReq.setItemIds(reqDTO.getItems().stream()
                .map(OrderCreateReqDTO.OrderItemCreateDTO::getProductId)
                .collect(Collectors.toList()));
        return validateReq;
    }

    private String generateOrderNo() {
        return "MT" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()) +
                String.format("%04d", new Random().nextInt(10000));
    }

    private String generatePickupCode() {
        return new Random().ints(4, 0, 10).mapToObj(String::valueOf).collect(Collectors.joining());
    }

    private void addOrderStatusTimeline(OrderEntity order, OrderStatus status, boolean isCurrent) {
        OrderStatusTimelineEntity timeline = new OrderStatusTimelineEntity();
        timeline.setOrder(order);
        timeline.setStatus(status.getCode());
        timeline.setStatusText(status.getDescription());
        timeline.setTime(Instant.now());
        timeline.setIsCurrent(isCurrent);
        timeline.setCreatedAt(Instant.now());
        orderStatusTimelineRepository.save(timeline);

        if (isCurrent) {
            orderStatusTimelineRepository.findByOrderIdAndIsCurrentTrue(order.getId())
                    .ifPresent(prev -> {
                        if (!prev.getId().equals(timeline.getId())) {
                            prev.setIsCurrent(false);
                            orderStatusTimelineRepository.save(prev);
                        }
                    });
        }
    }

    private BigDecimal calculateCouponDiscount(CouponTemplateEntity template, BigDecimal productAmount) {
        BigDecimal discount = BigDecimal.ZERO;
        if (template.getMinAmount().compareTo(productAmount) > 0) {
            return BigDecimal.ZERO;
        }

        switch (template.getType()) {
            case "discount":
                discount = template.getValue();
                break;
            case "percentage":
                discount = productAmount.multiply(template.getValue());
                break;
            case "fixed":
                discount = template.getValue();
                break;
            default:
                log.warn("Unknown coupon type: {}", template.getType());
                break;
        }
        return discount.min(productAmount);
    }

    private UserAddressResDTO.AddressDTO mapToAddressDTO(UserAddressEntity entity) {
        UserAddressResDTO.AddressDTO dto = new UserAddressResDTO.AddressDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setPhone(entity.getPhone());
        dto.setProvince(entity.getProvince());
        dto.setCity(entity.getCity());
        dto.setDistrict(entity.getDistrict());
        dto.setDetail(entity.getDetail());
        dto.setPostalCode(entity.getPostalCode());
        dto.setIsDefault(entity.getIsDefault());
        dto.setType(entity.getType());
        dto.setLabel(entity.getLabel());
        dto.setLongitude(entity.getLongitude());
        dto.setLatitude(entity.getLatitude());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
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
        dto.setLongitude(entity.getLongitude());
        dto.setLatitude(entity.getLatitude());
        return dto;
    }

    private CouponListResDTO.CouponDTO mapToCouponDTO(UserCouponEntity entity) {
        CouponListResDTO.CouponDTO dto = new CouponListResDTO.CouponDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getCouponTemplate().getName());
        dto.setType(entity.getCouponTemplate().getType());
        dto.setValue(entity.getCouponTemplate().getValue());
        dto.setMinAmount(entity.getCouponTemplate().getMinAmount());
        dto.setDescription(entity.getCouponTemplate().getDescription());
        dto.setUsage(entity.getCouponTemplate().getUsageScope());

        if (entity.getCouponTemplate().getTargetIds() != null) {
            try {
                // 使用 TypeReference 来避免未经检查的类型转换
                List<Long> targetIds = objectMapper.readValue(
                        entity.getCouponTemplate().getTargetIds(),
                        new TypeReference<List<Long>>() {}
                );
                dto.setTargetIds(targetIds);
            } catch (JsonProcessingException e) {
                log.error("Error parsing targetIds JSON: {}", entity.getCouponTemplate().getTargetIds(), e);
                dto.setTargetIds(Collections.emptyList());
            }
        } else {
            dto.setTargetIds(Collections.emptyList());
        }

        dto.setStatus(entity.getStatus());
        dto.setReceivedAt(entity.getReceivedAt());
        dto.setExpireAt(entity.getExpireAt());
        dto.setUsedAt(entity.getUsedAt());
        dto.setOrderId(entity.getOrderId());
        dto.setCanUse(true);
        dto.setUnusableReason("");
        return dto;
    }

    private OrderListResDTO.OrderSummaryDTO mapToOrderSummaryDTO(OrderEntity entity) {
        OrderListResDTO.OrderSummaryDTO dto = new OrderListResDTO.OrderSummaryDTO();
        dto.setId(entity.getId());
        dto.setOrderNo(entity.getOrderNo());
        dto.setStatus(entity.getStatus());
        dto.setStatusText(entity.getStatusText());
        dto.setType(entity.getType());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setPayAmount(entity.getPayAmount());
        dto.setItemCount(entity.getItems() != null ? entity.getItems().size() : 0);
        dto.setItems(entity.getItems() != null ? entity.getItems().stream()
                .map(this::mapToOrderItemBriefDTO)
                .collect(Collectors.toList()) : Collections.emptyList());
        dto.setStoreName(entity.getPickupStore() != null ? entity.getPickupStore().getName() : null);
        dto.setAddress(entity.getDeliveryAddress() != null ? entity.getDeliveryAddress().getDetail() : null);
        dto.setDeliveryTime(entity.getDeliveryTimeExpected());
        dto.setCreatedAt(entity.getCreatedAt());

        List<String> actions = new ArrayList<>();
        boolean needAction = false;
        if (OrderStatus.CREATED.getCode().equals(entity.getStatus()) && entity.getPayAmount().compareTo(BigDecimal.ZERO) > 0) {
            actions.add("pay");
            actions.add("cancel");
            needAction = true;
        } else if (OrderStatus.PAID.getCode().equals(entity.getStatus())) {
            actions.add("remind");
            actions.add("cancel");
            needAction = true;
        } else if (OrderStatus.MAKING.getCode().equals(entity.getStatus())) {
            actions.add("remind");
        } else if (OrderStatus.READY.getCode().equals(entity.getStatus()) || OrderStatus.DELIVERING.getCode().equals(entity.getStatus())) {
            actions.add("confirm");
            needAction = true;
        } else if (OrderStatus.COMPLETED.getCode().equals(entity.getStatus()) && !entity.getIsRated()) {
            actions.add("rate");
            needAction = true;
        }
        if (OrderStatus.COMPLETED.getCode().equals(entity.getStatus()) && entity.getRefundDeadline() != null && Instant.now().isBefore(entity.getRefundDeadline())) {
            actions.add("apply_refund");
            needAction = true;
        }
        dto.setActions(actions);
        dto.setNeedAction(needAction);

        return dto;
    }

    private OrderListResDTO.OrderItemBriefDTO mapToOrderItemBriefDTO(OrderItemEntity entity) {
        OrderListResDTO.OrderItemBriefDTO dto = new OrderListResDTO.OrderItemBriefDTO();
        dto.setProductName(entity.getProductName());
        dto.setProductImage(entity.getProductImageUrl());
        dto.setQuantity(entity.getQuantity());
        dto.setPrice(entity.getPriceAtOrder());
        return dto;
    }

    private OrderDetailResDTO mapToOrderDetailResDTO(OrderEntity entity, Long userId) {
        OrderDetailResDTO dto = new OrderDetailResDTO();
        dto.setId(entity.getId());
        dto.setOrderNo(entity.getOrderNo());
        dto.setStatus(entity.getStatus());
        dto.setStatusText(entity.getStatusText());
        dto.setType(entity.getType());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        dto.setItems(entity.getItems() != null ? entity.getItems().stream()
                .map(this::mapToOrderItemDetailDTO)
                .collect(Collectors.toList()) : Collections.emptyList());

        dto.setStatusTimeline(entity.getStatusTimelines() != null ? entity.getStatusTimelines().stream()
                .map(this::mapToOrderStatusTimelineDTO)
                .sorted(Comparator.comparing(OrderDetailResDTO.OrderStatusTimelineDTO::getTime, Comparator.nullsFirst(Comparator.naturalOrder())))
                .collect(Collectors.toList()) : Collections.emptyList());

        OrderDetailResDTO.OrderSummaryDetailDTO summaryDTO = new OrderDetailResDTO.OrderSummaryDetailDTO();
        summaryDTO.setProductAmount(entity.getProductAmount());
        summaryDTO.setDeliveryFee(entity.getDeliveryFee());
        summaryDTO.setPackageFee(entity.getPackageFee());
        summaryDTO.setDiscount(entity.getDiscountAmount());
        summaryDTO.setPointsDiscount(entity.getPointsUsed());
        summaryDTO.setBalanceDiscount(entity.getBalanceUsed());
        summaryDTO.setTotalAmount(entity.getTotalAmount());
        summaryDTO.setPayAmount(entity.getPayAmount());
        summaryDTO.setPointsEarned(entity.getTotalAmount().multiply(BigDecimal.TEN).intValue());
        dto.setSummary(summaryDTO);

        if ("delivery".equalsIgnoreCase(entity.getType()) && entity.getDeliveryAddress() != null) {
            dto.setAddress(mapToAddressDTO(entity.getDeliveryAddress()));
        }
        if (entity.getPickupStore() != null) {
            dto.setStore(mapToNearbyStoreDTO(entity.getPickupStore(), BigDecimal.ZERO, BigDecimal.ZERO, userId));
        }

        if ("delivery".equalsIgnoreCase(entity.getType())) {
            OrderDetailResDTO.DeliveryInfoDTO deliveryInfo = new OrderDetailResDTO.DeliveryInfoDTO();
            deliveryInfo.setDeliveryTime(entity.getDeliveryTimeExpected());
            deliveryInfo.setEstimatedArrival(entity.getEstimatedArrivalTime());
            deliveryInfo.setRiderName(entity.getRiderName());
            deliveryInfo.setRiderPhone(entity.getRiderPhone());
            if (entity.getRiderLongitude() != null && entity.getRiderLatitude() != null) {
                deliveryInfo.setRiderLocation(new OrderDetailResDTO.RiderLocationDTO(entity.getRiderLongitude(), entity.getRiderLatitude()));
            }
            dto.setDeliveryInfo(deliveryInfo);
        } else if ("pickup".equalsIgnoreCase(entity.getType())) {
            OrderDetailResDTO.PickupInfoDTO pickupInfo = new OrderDetailResDTO.PickupInfoDTO();
            pickupInfo.setPickupCode(entity.getPickupCode());
            pickupInfo.setPickupTime(entity.getPickupTimeActual());
            pickupInfo.setEstimatedReadyTime(entity.getEstimatedReadyTime());
            pickupInfo.setCounterNumber(entity.getCounterNumber());
            dto.setPickupInfo(pickupInfo);
        }

        paymentRepository.findByOrderId(entity.getId()).ifPresent(paymentEntity -> {
            OrderDetailResDTO.PaymentDetailDTO paymentDTO = new OrderDetailResDTO.PaymentDetailDTO();
            paymentDTO.setPayType(paymentEntity.getPayType());
            paymentDTO.setPayAmount(paymentEntity.getPayAmount());
            paymentDTO.setPayTime(paymentEntity.getPayTime());
            paymentDTO.setTransactionId(paymentEntity.getTransactionId());
            dto.setPayment(paymentDTO);
        });

        if (entity.getCouponId() != null) {
            userCouponRepository.findById(entity.getCouponId()).ifPresent(userCoupon -> {
                dto.setCoupon(mapToCouponDTO(userCoupon));
            });
        }

        dto.setPointsUsed(entity.getPointsUsed());
        dto.setBalanceUsed(entity.getBalanceUsed());
        dto.setRemark(entity.getRemark());

        if (entity.getInvoiceType() != null) {
            OrderDetailResDTO.InvoiceDetailDTO invoiceDTO = new OrderDetailResDTO.InvoiceDetailDTO();
            invoiceDTO.setType(entity.getInvoiceType());
            invoiceDTO.setTitle(entity.getInvoiceTitle());
            invoiceDTO.setStatus("pending");
            dto.setInvoice(invoiceDTO);
        }

        List<String> actions = new ArrayList<>();
        if (OrderStatus.CREATED.getCode().equals(entity.getStatus()) || OrderStatus.PAID.getCode().equals(entity.getStatus())) {
            if (entity.getCancelDeadline() == null || Instant.now().isBefore(entity.getCancelDeadline())) {
                actions.add("cancel");
            }
            if (OrderStatus.PAID.getCode().equals(entity.getStatus())) {
                actions.add("remind");
            }
        }
        if (OrderStatus.MAKING.getCode().equals(entity.getStatus())) {
            actions.add("remind");
        }
        if (OrderStatus.READY.getCode().equals(entity.getStatus()) || OrderStatus.DELIVERING.getCode().equals(entity.getStatus())) {
            actions.add("confirm");
        }
        if (OrderStatus.COMPLETED.getCode().equals(entity.getStatus())) {
            if (entity.getRefundDeadline() != null && Instant.now().isBefore(entity.getRefundDeadline())) {
                actions.add("apply_refund");
            }
            if (!entity.getIsRated() && (entity.getRateDeadline() == null || Instant.now().isBefore(entity.getRateDeadline()))) {
                actions.add("rate");
            }
        }
        dto.setActions(actions);
        dto.setCancelDeadline(entity.getCancelDeadline());
        dto.setRefundDeadline(entity.getRefundDeadline());
        dto.setRateDeadline(entity.getRateDeadline());

        return dto;
    }

    private OrderDetailResDTO.OrderItemDetailDTO mapToOrderItemDetailDTO(OrderItemEntity entity) {
        OrderDetailResDTO.OrderItemDetailDTO dto = new OrderDetailResDTO.OrderItemDetailDTO();
        dto.setProductId(entity.getProduct().getId());
        dto.setProductName(entity.getProductName());
        dto.setProductImage(entity.getProductImageUrl());
        dto.setQuantity(entity.getQuantity());
        dto.setPrice(entity.getPriceAtOrder());
        dto.setOriginalPrice(entity.getOriginalPriceAtOrder());
        dto.setSubtotal(entity.getSubtotal());

        OrderDetailResDTO.CustomizationsDetailDTO customizationsDTO = new OrderDetailResDTO.CustomizationsDetailDTO();
        List<OrderDetailResDTO.ToppingDetailDTO> toppingDetailDTOs = new ArrayList<>();
        for (OrderItemCustomizationEntity customization : entity.getCustomizations()) {
            switch (customization.getCustomizationTypeName()) {
                case "sweetness" -> customizationsDTO.setSweetness(customization.getOptionLabel());
                case "temperature" -> customizationsDTO.setTemperature(customization.getOptionLabel());
                case "toppings" -> toppingDetailDTOs.add(new OrderDetailResDTO.ToppingDetailDTO(
                        customization.getOptionLabel(), customization.getPriceAdjustmentAtOrder(), customization.getQuantity()
                ));
            }
        }
        customizationsDTO.setToppings(toppingDetailDTOs);
        dto.setCustomizations(customizationsDTO);
        return dto;
    }

    private OrderDetailResDTO.OrderStatusTimelineDTO mapToOrderStatusTimelineDTO(OrderStatusTimelineEntity entity) {
        OrderDetailResDTO.OrderStatusTimelineDTO dto = new OrderDetailResDTO.OrderStatusTimelineDTO();
        dto.setStatus(entity.getStatus());
        dto.setText(entity.getStatusText());
        dto.setTime(entity.getTime());
        dto.setCompleted(entity.getIsCurrent());
        dto.setCurrent(entity.getIsCurrent());
        dto.setEstimatedTime(null);
        return dto;
    }

    private Long getOptionIdFromValue(String optionValue) {
        log.warn("Directly mapping option value '{}' to a dummy ID. This needs proper implementation.", optionValue);
        return 1L;
    }
    private HomePageResDTO.ProductItemDTO mapToProductItemDTO(ProductEntity entity) {
        HomePageResDTO.ProductItemDTO dto = new HomePageResDTO.ProductItemDTO();
        dto.setId(String.valueOf(entity.getId()));
        dto.setName(entity.getName());
        dto.setImage(entity.getMainImageUrl());
        dto.setPrice(entity.getPrice());
        dto.setOriginalPrice(entity.getOriginalPrice());
        dto.setSales(entity.getSales());

        // 修复：使用 TypeReference 避免未经检查的类型转换
        if (entity.getTags() != null) {
            try {
                List<String> tags = objectMapper.readValue(
                        entity.getTags(),
                        new TypeReference<List<String>>() {}
                );
                dto.setTags(tags);
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