package com.example.vestrapay.business.repository;

import com.example.vestrapay.business.models.Business;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface BusinessRepository extends R2dbcRepository<Business,Long> {
    Mono<Business> findBusinessByMerchantId(String merchantId);
}
