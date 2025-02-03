package com.example.vestrapay.merchant.transactions.interfaces;

import com.example.vestrapay.merchant.dashboard.models.Balance;
import com.example.vestrapay.merchant.transactions.dtos.TransactionRangeDTO;
import com.example.vestrapay.merchant.transactions.models.Transaction;
import com.example.vestrapay.utils.dtos.Response;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface ITransactionService {
    Mono<Response<Transaction>> getTransactionByParam(Map<String,Object> request);
    Mono<Response<List<Transaction>>> getTransactionsByParam(Map<String,Object> request);
    Mono<Response<List<Transaction>>> getTopTenTransactions(Map<String,Object> request);
    Mono<Response<List<Transaction>>> adminGetAllTransactions(Map<String,Object> request);
    Mono<Response<List<Transaction>>> getTop10TransactionForUser();


    Mono<Response<List<Transaction>>> getDailyTransactions();


    //Merchant services
    Mono<Response<List<Transaction>>> merchantGetAll();
    Mono<Response<List<Transaction>>> getTransactionsByRange(TransactionRangeDTO request);
    Mono<Response<Transaction>> getByTransactionReference(String transactionReference);
    Mono<Response<Transaction>> getByUuid(String uuid);


    Mono<Response<Balance>> getUnSettledTransactions(String currency);
}
