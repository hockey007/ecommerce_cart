package com.ecommerce.cart.repository;

import com.ecommerce.cart.model.Cart;
import com.ecommerce.cart.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    Optional<CartItem> findByProductIdAndVariantId(UUID productId, UUID variantId);
    List<CartItem> findByCart(Cart cart);
    Boolean deleteByCartId(UUID cartId);
}
