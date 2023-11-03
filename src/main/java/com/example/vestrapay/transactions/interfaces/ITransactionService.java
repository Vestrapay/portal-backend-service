package com.example.vestrapay.transactions.interfaces;

import com.example.vestrapay.transactions.models.Transaction;
import com.example.vestrapay.utils.dtos.Response;
import io.r2dbc.spi.Result;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ITransactionService {
    Mono<Response<Transaction>> getTransactionByParam(Map<String,Object> request);
    Mono<Response<List<Transaction>>> getTransactionsByParam(Map<String,Object> request);
    Mono<Response<List<Transaction>>> getTopTenTransactions(Map<String,Object> request);
    Mono<Response<List<Transaction>>> adminGetAllTransactions(Map<String,Object> request);
    Mono<Response<List<Transaction>>> getTop10TransactionForUser();


    Mono<Response<List<Transaction>>> getDailyTransactions();
}
