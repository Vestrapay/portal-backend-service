package com.example.vestrapay.merchant.dispute.repository;

import com.example.vestrapay.merchant.dispute.entity.Dispute;
import com.example.vestrapay.merchant.dispute.enums.DisputeEnum;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DisputeRepository extends R2dbcRepository<Dispute,Long> {
    Flux<Dispute> findByMerchantIdAndTransactionReference(String merchantId, String transactionReference);
    Mono<Dispute> findByMerchantIdAndUuid(String merchantId, String uuid);
    Flux<Dispute> findByMerchantId(String merchantId);
    Flux<Dispute>findByStatusOrStatus(DisputeEnum opened,DisputeEnum pending);
    Flux<Dispute>findByMerchantIdAndStatusOrStatus(String merchantId, DisputeEnum opened,DisputeEnum pending);
    Mono<Dispute>findByUuid(String disputeId);

}
