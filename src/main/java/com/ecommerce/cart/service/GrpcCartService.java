package com.ecommerce.cart.service;

import com.ecommerce.common.CartServiceGrpc;
import com.ecommerce.common.GetCartRequest;
import com.ecommerce.common.GetCartResponse;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;

public class GrpcCartService extends CartServiceGrpc.CartServiceImplBase {

    @Autowired
    private CartService cartService;

    @Override
    public void getCartItems(
            GetCartRequest cartRequest,
            StreamObserver<GetCartResponse> responseStreamObserver
    ) {
        GetCartResponse cartResponse = cartService.getCartItems(cartRequest.getUserId());
        responseStreamObserver.onNext(cartResponse);
        responseStreamObserver.onCompleted();
    }

}
