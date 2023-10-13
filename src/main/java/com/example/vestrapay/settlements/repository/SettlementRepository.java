package com.example.vestrapay.settlements.repository;

import com.example.vestrapay.settlements.models.Settlement;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SettlementRepository extends R2dbcRepository<Settlement,Long> {
    Mono<Settlement>findByAccountNumberAndMerchantId(String accountNumber,String userUUID);
    Mono<Settlement>findByUuidAndMerchantId(String uuid,String userUUID);
    Mono<Settlement>findByMerchantIdAndPrimaryAccount(String uuid,boolean isPrimary);
    @Modifying
    @Query("update settlement set primary_account=false where merchant_id=:1")
    Mono<Object>resetPrimarySettlementToFalseForUser(@Param("user_id") String userUUID);

    Flux<Settlement> findByMerchantId(String merchantId);
}
