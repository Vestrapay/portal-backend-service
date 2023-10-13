package com.example.vestrapay.paymentmethods.interfaces;

import com.example.vestrapay.utils.dtos.Response;
import reactor.core.publisher.Mono;

public interface IPaymentMethodInterface {
    Mono<Response<?>>getAllMethods();
}
