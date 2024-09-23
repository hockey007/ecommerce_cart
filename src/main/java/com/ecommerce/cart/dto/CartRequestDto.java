package com.ecommerce.cart.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartRequestDto {

    @JsonProperty("user_id")
    @NotBlank(message = "UserId is required")
    private String userId;

    @JsonProperty("product_id")
    @NotBlank(message = "ProductId is required")
    private String productId;

    @JsonProperty("variant_id")
    @NotBlank(message = "VariantId is required")
    private String variantId;

    @Min(value = 1, message = "Invalid quantity")
    private Integer quantity;

}
