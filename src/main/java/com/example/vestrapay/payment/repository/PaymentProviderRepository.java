package com.example.vestrapay.payment.repository;

import com.example.vestrapay.payment.model.PaymentProviders;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface PaymentProviderRepository extends R2dbcRepository<PaymentProviders,Long> {
    Mono<PaymentProviders>findByName(String name);
    Mono<PaymentProviders>findByUuid(String name);
}
