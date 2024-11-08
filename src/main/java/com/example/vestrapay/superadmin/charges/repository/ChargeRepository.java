package com.example.vestrapay.superadmin.charges.repository;

import com.example.vestrapay.superadmin.charges.enums.ChargeCategory;
import com.example.vestrapay.superadmin.charges.enums.PaymentMethod;
import com.example.vestrapay.superadmin.charges.model.Charge;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChargeRepository extends R2dbcRepository<Charge,Long> {
    Mono<Charge> findByMerchantIdAndPaymentMethodAndCategory(String merchantId, PaymentMethod paymentMethod, ChargeCategory category);
    Flux<Charge> findAllByMerchantId(String merchantId);
    Mono<Charge> findByUuid(String chargeId);
}
