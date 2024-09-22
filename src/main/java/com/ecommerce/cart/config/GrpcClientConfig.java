package com.ecommerce.cart.config;

import inventory.InventoryServiceGrpc.InventoryServiceBlockingStub;
import inventory.InventoryServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import product.ProductServiceGrpc;

@Configuration
public class GrpcClientConfig {

    @Bean
    @Qualifier("inventoryChannel")
    public ManagedChannel inventoryServiceChannel() {
        return ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();
    }

    @Bean
    @Qualifier("productChannel")
    public ManagedChannel productServiceChannel() {
        return ManagedChannelBuilder.forAddress("localhost", 8079)
                .usePlaintext()
                .build();
    }

    @Bean
    public InventoryServiceBlockingStub inventoryServiceBlockingStub(
            @Qualifier("inventoryChannel") ManagedChannel inventoryChannel
    ) {
        return InventoryServiceGrpc.newBlockingStub(inventoryChannel);
    }

    @Bean
    public ProductServiceGrpc.ProductServiceBlockingStub productServiceBlockingStub(
            @Qualifier("productChannel") ManagedChannel productChannel
    ) {
        return ProductServiceGrpc.newBlockingStub(productChannel);
    }

}
