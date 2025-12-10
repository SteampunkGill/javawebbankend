// File: milktea-backend/src/main/java/com.milktea.app/service/CartService.java
package com.milktea.app.service;

import com.milktea.app.dto.cart.CartBatchOperationReqDTO;
import com.milktea.app.dto.cart.CartItemAddReqDTO;
import com.milktea.app.dto.cart.CartItemUpdateReqDTO;
import com.milktea.app.dto.cart.CartResDTO;

public interface CartService {
    CartResDTO getCart(Long userId);
    CartResDTO addCartItem(Long userId, CartItemAddReqDTO reqDTO);
    CartResDTO updateCartItem(Long userId, Long cartItemId, CartItemUpdateReqDTO reqDTO);
    CartResDTO deleteCartItem(Long userId, Long cartItemId);
    CartResDTO batchOperateCartItems(Long userId, CartBatchOperationReqDTO reqDTO);
    void clearCart(Long userId);
}