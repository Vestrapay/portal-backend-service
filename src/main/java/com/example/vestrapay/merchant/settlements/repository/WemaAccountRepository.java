package com.example.vestrapay.merchant.settlements.repository;

import com.example.vestrapay.merchant.settlements.models.WemaAccounts;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface WemaAccountRepository extends R2dbcRepository<WemaAccounts,Long> {
    Mono<WemaAccounts>findByMerchantId(String merchantId);
}
