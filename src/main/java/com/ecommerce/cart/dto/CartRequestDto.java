package com.ecommerce.cart.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartRequestDto {

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("product_id")
    private String productId;

    @JsonProperty("variant_id")
    private String variantId;

    private Integer quantity;

}
