package com.example.vestrapay.transactions.controllers;

import com.example.vestrapay.transactions.interfaces.ITransactionService;
import com.example.vestrapay.transactions.models.Transaction;
import com.example.vestrapay.utils.dtos.Response;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v1/transactions")
@Tag(name = "TRANSACTIONS", description = "Transaction Management")
@SecurityRequirement(name = "vestrapay")
@CrossOrigin(origins ="*",maxAge = 3600)
@RequiredArgsConstructor
public class TransactionController {
    private final ITransactionService transactionService;

    @PostMapping("get-one-transaction")
    public Mono<ResponseEntity<Response<Transaction>>> getOneTransaction(@RequestBody Map<String,Object > request){
        return transactionService.getTransactionByParam(request)
                .map(userResponse -> ResponseEntity.status(userResponse.getStatus()).body(userResponse));
    }

    @PostMapping("get-transactions")
    public Mono<ResponseEntity<Response<List<Transaction>>>> getTransactionsByParam(@RequestBody Map<String,Object > request){
        return transactionService.getTransactionsByParam(request)
                .map(userResponse -> ResponseEntity.status(userResponse.getStatus()).body(userResponse));
    }


    @PostMapping("get-top-ten")
    public Mono<ResponseEntity<Response<List<Transaction>>>> getTopTenTransactions(@RequestBody Map<String,Object > request){
        return transactionService.getTopTenTransactions(request)
                .map(userResponse -> ResponseEntity.status(userResponse.getStatus()).body(userResponse));
    }

    @PostMapping("get-all-transactions")
    public Mono<ResponseEntity<Response<List<Transaction>>>> AdminGetAll(@RequestBody Map<String,Object > request){
        return transactionService.adminGetAllTransactions(request)
                .map(userResponse -> ResponseEntity.status(userResponse.getStatus()).body(userResponse));
    }
}
