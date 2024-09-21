package com.ecommerce.cart.controller;

import com.ecommerce.cart.dto.CartRequestDto;
import com.ecommerce.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    public void addToCart(@RequestBody CartRequestDto cartRequestDto) {
        cartService.addToCart(
                cartRequestDto.getUserId(),
                cartRequestDto.getProductId(),
                cartRequestDto.getVariantId(),
                cartRequestDto.getQuantity()
        );
    }

}
