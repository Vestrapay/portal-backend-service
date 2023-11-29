package com.example.vestrapay.dispute.repository;

import com.example.vestrapay.dispute.entity.Dispute;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DisputeRepository extends R2dbcRepository<Dispute,Long> {
    Flux<Dispute> findByMerchantIdAndTransactionReference(String merchantId, String transactionReference);
    Mono<Dispute> findByMerchantIdAndUuid(String merchantId, String uuid);
    Flux<Dispute> findByMerchantId(String merchantId);
}
