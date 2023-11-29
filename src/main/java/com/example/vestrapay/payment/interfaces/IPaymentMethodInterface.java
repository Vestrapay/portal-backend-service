package com.example.vestrapay.payment.interfaces;

import com.example.vestrapay.payment.dtos.PaymentProvidersDTO;
import com.example.vestrapay.payment.dtos.RouteRuleDTO;
import com.example.vestrapay.payment.model.RoutingRule;
import com.example.vestrapay.utils.dtos.Response;
import reactor.core.publisher.Mono;

public interface IPaymentMethodInterface {
    Mono<Response<?>>getAllMethods();
    Mono<Response<Object>>createPaymentMethod(String name);
    Mono<Response<Object>> registerProvider(PaymentProvidersDTO request);
    Mono<Response<Object>> updateProvider(PaymentProvidersDTO request);
    Mono<Response<Object>> viewAllProviders();
    Mono<Response<Object>> viewAllProvidersByPaymentMethod(String paymentMethod);
    Mono<Response<Object>> viewProviderByUuid(String uuid);
    Mono<Response<Object>> configureRoute(RouteRuleDTO request);
    Mono<Response<Object>> updateRoute(RouteRuleDTO request);

}
