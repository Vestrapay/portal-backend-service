package com.example.vestrapay.transactions.reporitory;

import com.example.vestrapay.transactions.models.Transaction;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface TransactionRepository extends R2dbcRepository<Transaction,Long> {
    @Query("select * from vestrapay_transactions where merchant_id = $1 ORDER BY id DESC LIMIT 10")
    Flux<Transaction>findTop10(String userId);

    Flux<Transaction>findTransactionsByMerchantIdAndCreatedAtBetween(String merchantId, LocalDateTime startTime,LocalDateTime endTime);

    Flux<Transaction>findByMerchantId(String merchantId);
    Mono<Transaction> findByMerchantIdAndTransactionReference(String merchantId,String transactionReference);
}
