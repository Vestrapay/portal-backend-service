package com.example.vestrapay.payment.repository;

import com.example.vestrapay.payment.model.RoutingRule;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface RoutingRuleRepository extends R2dbcRepository<RoutingRule,Long> {
    Mono<RoutingRule>findByPaymentMethod(String paymentMethod);
}
