package com.example.vestrapay.merchant.transactions.reporitory;

import com.example.vestrapay.merchant.transactions.enums.Status;
import com.example.vestrapay.merchant.transactions.models.Transaction;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface TransactionRepository extends R2dbcRepository<Transaction,Long> {
    @Query("select * from vestrapay_transactions where merchant_id = $1 ORDER BY id DESC LIMIT 10")
    Flux<Transaction>findTop10(String userId);

    @Query("select * from vestrapay_transactions ORDER BY id DESC LIMIT 10")
    Flux<Transaction>findTopTransactionsForAdmin();

    @Query("select * from vestrapay_transactions ORDER BY id DESC")
    Flux<Transaction>findAllTransactions();

    Flux<Transaction>findTransactionsByMerchantIdAndCreatedAtBetween(String merchantId, LocalDateTime startTime,LocalDateTime endTime);
    Flux<Transaction>findTransactionsByCreatedAtBetween(LocalDateTime startTime,LocalDateTime endTime);
    @Query("select * from vestrapay_transactions where merchant_id = $1 ORDER BY id DESC")
    Flux<Transaction>findByMerchantId(String merchantId);
    Mono<Transaction> findByMerchantIdAndTransactionReference(String merchantId,String transactionReference);
    Mono<Transaction> findByMerchantIdAndUuid(String merchantId,String uuid);

    Flux<Transaction>findAllByMerchantIdAndTransactionStatusAndSettlementStatus(String merchantId, Status transactionStatus,Status settlementStatus);
    Flux<Transaction>findAllByTransactionStatusAndSettlementStatus(Status transactionStatus,Status settlementStatus);
}
