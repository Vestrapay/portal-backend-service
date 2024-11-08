package com.example.vestrapay.superadmin.payment.repository;

import com.example.vestrapay.superadmin.payment.model.PaymentMethods;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface PaymentMethodRepository extends R2dbcRepository<PaymentMethods,Long> {
    Mono<PaymentMethods>findByName(String name);
}
