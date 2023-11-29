package com.example.vestrapay.payment.repository;

import com.example.vestrapay.payment.model.PaymentMethods;
import com.example.vestrapay.payment.model.PaymentProviders;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface PaymentMethodRepository extends R2dbcRepository<PaymentMethods,Long> {
    Mono<PaymentMethods>findByName(String name);
}
