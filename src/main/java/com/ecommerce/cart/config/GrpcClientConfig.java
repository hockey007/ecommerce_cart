package com.ecommerce.cart.config;

import inventory.InventoryServiceGrpc.InventoryServiceBlockingStub;
import inventory.InventoryServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

    @Bean
    public ManagedChannel inventoryServiceChannel() {
        return ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();
    }

    @Bean
    public InventoryServiceBlockingStub inventoryServiceBlockingStub(ManagedChannel channel) {
        return InventoryServiceGrpc.newBlockingStub(channel);
    }

}
