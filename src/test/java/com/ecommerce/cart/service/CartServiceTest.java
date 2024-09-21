package com.ecommerce.cart.service;

import com.ecommerce.cart.repository.CartItemRepository;
import com.ecommerce.cart.repository.CartRepository;
import inventory.InventoryProto;
import inventory.InventoryServiceGrpc.InventoryServiceBlockingStub;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CartServiceTest {

    @InjectMocks
    private CartService cartService;

    @Mock
    private InventoryServiceBlockingStub inventoryService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    private final String TEST_USER_ID = "b173b1d8-2245-4cbc-a794-bbe89a7d3b54";
    private final String TEST_PRODUCT_ID = "e5dbe9d0-5990-4e91-853c-fdea725f233b";
    private final String TEST_VARIANT_ID = "1c665311-3383-49cc-92f7-c591291da8c5";

    private final String INSUFFICIENT_STOCK = "Insufficient stock";

    @Test
    void testAddToCartThrowsIllegalStateExceptionWhenAvailableQuantityIsZero() {
        when(inventoryService.getStock(any(InventoryProto.InventoryRequest.class)))
                .thenReturn(InventoryProto.InventoryStockResponse.newBuilder().setStock(0).build());

        Exception exception = assertThrows(
                IllegalStateException.class,
                () -> cartService.addToCart(TEST_USER_ID, TEST_PRODUCT_ID, TEST_VARIANT_ID, null)
        );

        assertEquals(INSUFFICIENT_STOCK, exception.getMessage());
    }

    @Test
    void testAddToCartThrowsExceptionWhenAvailableQuantityIsInsufficient() {
        when(inventoryService.getStock(any(InventoryProto.InventoryRequest.class)))
                .thenReturn(InventoryProto.InventoryStockResponse.newBuilder().setStock(5).build());

        Exception exception = assertThrows(
                IllegalStateException.class,
                () -> cartService.addToCart(TEST_USER_ID, TEST_PRODUCT_ID, TEST_VARIANT_ID, 10)
        );

        assertEquals(INSUFFICIENT_STOCK, exception.getMessage());
    }

}