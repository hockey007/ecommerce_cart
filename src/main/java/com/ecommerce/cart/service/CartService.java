package com.ecommerce.cart.service;

import cart.CartProto;
import cart.CartProto.ClearCartResponse;
import com.ecommerce.cart.model.Cart;
import com.ecommerce.cart.model.CartItem;
import com.ecommerce.cart.repository.CartItemRepository;
import com.ecommerce.cart.repository.CartRepository;
import inventory.InventoryProto.InventoryRequest;
import inventory.InventoryProto.InventoryStockResponse;
import inventory.InventoryServiceGrpc.InventoryServiceBlockingStub;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import product.ProductProto;
import product.ProductServiceGrpc.ProductServiceBlockingStub;
import cart.CartProto.GetCartResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.UUID.fromString;

@Service
public class CartService {

    @Autowired
    private InventoryServiceBlockingStub inventoryServiceBlockingStub;

    @Autowired
    private ProductServiceBlockingStub productServiceBlockingStub;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    private static final Integer INITIAL_CART_VALUE = 1;

    @Transactional
    public ClearCartResponse clearCart(String userId) {
        ClearCartResponse.Builder cartResponseBuilder = ClearCartResponse.newBuilder();
        try {
            UUID _userId = fromString(userId);
            Cart cart = cartRepository.findByUserId(_userId)
                    .orElseThrow(() -> new IllegalStateException("Cart not found for UserId"));

            Boolean cleared = cartItemRepository.deleteByCartId(cart.getId());
            if(!cleared) {
                throw new IllegalStateException("Something went wrong");
            }

            cartRepository.delete(cart);
            cartResponseBuilder.setSuccess(true);
        } catch (Exception e) {
            cartResponseBuilder.setMessage(e.getMessage());
        }

        return cartResponseBuilder.build();
    }

    public GetCartResponse getCartItems(String userId) {
        Cart cart = cartRepository.findByUserId(fromString(userId))
                .orElseThrow(() -> new IllegalStateException("Cart not found for user"));

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
        GetCartResponse.Builder cartResponse = GetCartResponse.newBuilder();
        for(CartItem cartItem: cartItems) {
            CartProto.CartItem grpcCartItem = CartProto.CartItem.newBuilder()
                    .setProductId(cartItem.getProductId().toString())
                    .setVariantId(cartItem.getVariantId().toString())
                    .setQuantity(cartItem.getQuantity())
                    .build();

            cartResponse.addCartItems(grpcCartItem);
        }

        return cartResponse.build();
    }

    public void addToCart(String userId, String productId, String variantId, Integer quantity) {
        validateProduct(productId, variantId);
        Integer availableStock = getStockAvailability(productId, variantId, quantity);

        UUID _userId = fromString(userId);
        Cart cart = cartRepository.findByUserId(_userId).orElseGet(() -> createNewCart(_userId));

        UUID _productId = fromString(productId);
        UUID _variantId = fromString(variantId);
        addOrUpdateToCart(cart, _productId, _variantId, availableStock, quantity);
    }

    public void removeFromCart(String userId, String productId, String variantId) {
        UUID _userId = fromString(userId);
        Cart cart = cartRepository.findByUserId(_userId)
                .orElseThrow(() -> new IllegalStateException("Cart not found for User"));

        UUID _productId = fromString(productId);
        UUID _variantId = fromString(variantId);
        CartItem cartItem = cartItemRepository
                .findByProductIdAndVariantId(_productId, _variantId)
                .orElseThrow(() -> new IllegalStateException("Product not found in cart"));

        // do an availability check
        // explore how to handle abandoned cart with higher quantity than available
        cartItem.setQuantity(cartItem.getQuantity() - 1);
        cartItemRepository.save(cartItem);
    }

    public void deleteFromCart(String userId, String productId, String variantId) {
        UUID _userId = fromString(userId);
        Cart cart = cartRepository.findByUserId(_userId)
                .orElseThrow(() -> new IllegalStateException("Cart not found for User"));

        UUID _productId = fromString(productId);
        UUID _variantId = fromString(variantId);
        CartItem cartItem = cartItemRepository.findByProductIdAndVariantId(_productId, _variantId)
                .orElseThrow(() -> new IllegalStateException("Product not found in cart"));

        cartItemRepository.delete(cartItem);

        if(cart.getCount() > 0) {
            cart.setCount(cart.getCount() - 1);
        }

        if (cart.getCount() == 0) {
            cartRepository.delete(cart);
        } else {
            cartRepository.save(cart);
        }
    }

    private void validateProduct(String productId, String variantId) {
        ProductProto.ProductRequest productRequest = ProductProto.ProductRequest.newBuilder()
                .setProductId(productId)
                .setVariantId(variantId)
                .build();

        ProductProto.ProductResponse productResponse = productServiceBlockingStub.validateProduct(productRequest);
        System.out.println(productResponse);

        if(productResponse.getError() && !productResponse.getValid()) {
            throw new IllegalStateException(productResponse.getMessage());
        }
    }

    private void addOrUpdateToCart(Cart cart, UUID productId, UUID variantId, Integer availableStock, Integer quantity) {
        Optional<CartItem> optionalCartItem = cartItemRepository.findByProductIdAndVariantId(productId, variantId);
        if(optionalCartItem.isEmpty()) {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProductId(productId);
            cartItem.setVariantId(variantId);
            cartItem.setQuantity(quantity != null ? quantity : INITIAL_CART_VALUE);
            cartItemRepository.save(cartItem);

            cart.setCount(cart.getCount() + 1);
            cartRepository.save(cart);
        } else {
            CartItem cartItem = optionalCartItem.get();

            if(cartItem.getQuantity() > availableStock) throw new IllegalStateException("Insufficient stock");

            cartItem.setQuantity(quantity != null ? quantity : cartItem.getQuantity() + 1);
            cartItemRepository.save(cartItem);
        }
    }

    private Integer getStockAvailability(String productId, String variantId, Integer quantity) {
        InventoryRequest inventoryRequest = InventoryRequest.newBuilder()
                .setProductId(productId)
                .setVariantId(variantId)
                .setLocation("Test")
                .build();

        InventoryStockResponse inventoryStockResponse = inventoryServiceBlockingStub.getStock(inventoryRequest);
        Integer availableStock = inventoryStockResponse.getStock();

        if (availableStock == 0 || (quantity != null && availableStock < quantity)) {
            throw new IllegalStateException("Insufficient stock");
        }

        return availableStock;
    }

    private Cart createNewCart(UUID userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);

        return cartRepository.save(cart);
    }

}
