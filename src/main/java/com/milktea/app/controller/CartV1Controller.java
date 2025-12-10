// File: milktea-backend/src/main/java/com.milktea.app/controller/CartV1Controller.java
package com.milktea.app.controller;

import com.milktea.app.common.ApiResponse;
import com.milktea.app.dto.cart.CartBatchOperationReqDTO;
import com.milktea.app.dto.cart.CartItemAddReqDTO;
import com.milktea.app.dto.cart.CartItemUpdateReqDTO;
import com.milktea.app.dto.cart.CartResDTO;
import com.milktea.app.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/cart") // Base path for cart module
@RequiredArgsConstructor
@Slf4j
public class CartV1Controller {

    private final CartService cartService;

    private Long getUserId(@AuthenticationPrincipal User principal) {
        return Long.parseLong(principal.getUsername());
    }

    @GetMapping // Matches /cart
    public ApiResponse<CartResDTO> getCart(@AuthenticationPrincipal User principal) {
        Long userId = getUserId(principal);
        log.info("Getting cart for user: {}", userId);
        CartResDTO resDTO = cartService.getCart(userId);
        return ApiResponse.success(resDTO);
    }

    @PostMapping("/items") // Matches /cart/items
    public ApiResponse<CartResDTO> addCartItem(@AuthenticationPrincipal User principal,
                                               @Valid @RequestBody CartItemAddReqDTO reqDTO) {
        Long userId = getUserId(principal);
        log.info("Adding cart item for user {}: {}", userId, reqDTO.getProductId());
        CartResDTO resDTO = cartService.addCartItem(userId, reqDTO);
        return ApiResponse.success(resDTO);
    }

    @PutMapping("/items/{cartItemId}") // Matches /cart/items/{id}
    public ApiResponse<CartResDTO> updateCartItem(@AuthenticationPrincipal User principal,
                                                  @PathVariable("cartItemId") Long cartItemId, // Renamed path variable for clarity
                                                  @Valid @RequestBody CartItemUpdateReqDTO reqDTO) {
        Long userId = getUserId(principal);
        log.info("Updating cart item {} for user {}: {}", cartItemId, userId, reqDTO.getQuantity());
        CartResDTO resDTO = cartService.updateCartItem(userId, cartItemId, reqDTO);
        return ApiResponse.success(resDTO);
    }

    @DeleteMapping("/items/{cartItemId}") // Matches /cart/items/{id}
    public ApiResponse<CartResDTO> deleteCartItem(@AuthenticationPrincipal User principal,
                                                  @PathVariable("cartItemId") Long cartItemId) { // Renamed path variable for clarity
        Long userId = getUserId(principal);
        log.info("Deleting cart item {} for user {}", cartItemId, userId);
        CartResDTO resDTO = cartService.deleteCartItem(userId, cartItemId);
        return ApiResponse.success(resDTO);
    }

    @PostMapping("/batch") // Matches /cart/batch
    public ApiResponse<CartResDTO> batchOperateCartItems(@AuthenticationPrincipal User principal,
                                                         @Valid @RequestBody CartBatchOperationReqDTO reqDTO) {
        Long userId = getUserId(principal);
        log.info("Batch operation '{}' on cart for user {}", reqDTO.getAction(), userId);
        CartResDTO resDTO = cartService.batchOperateCartItems(userId, reqDTO);
        return ApiResponse.success(resDTO);
    }

    @DeleteMapping // Matches /cart
    public ApiResponse<Void> clearCart(@AuthenticationPrincipal User principal) {
        Long userId = getUserId(principal);
        log.info("Clearing cart for user {}", userId);
        cartService.clearCart(userId);
        return ApiResponse.success();
    }
}