package com.example.vestrapay.merchant.transactions.controllers;

import com.example.vestrapay.merchant.transactions.dtos.TransactionRangeDTO;
import com.example.vestrapay.merchant.transactions.interfaces.ITransactionService;
import com.example.vestrapay.merchant.transactions.models.Transaction;
import com.example.vestrapay.utils.dtos.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
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
    @GetMapping("get-one/{uuid}")
    public Mono<ResponseEntity<Response<Transaction>>> getByUUID(@PathVariable("uuid") String uuid){
        return transactionService.getByUuid(uuid)
                .map(userResponse -> ResponseEntity.status(userResponse.getStatus()).body(userResponse));
    }

    @GetMapping("get-one-by-reference/{transactionReference}")
    public Mono<ResponseEntity<Response<Transaction>>> getByTransactionReference(@PathVariable("transactionReference") String transactionReference){
        return transactionService.getByTransactionReference(transactionReference)
                .map(userResponse -> ResponseEntity.status(userResponse.getStatus()).body(userResponse));
    }

    @PostMapping("get-range")
    public Mono<ResponseEntity<Response<List<Transaction>>>> getTransactionsByRange(@RequestBody TransactionRangeDTO request){
        return transactionService.getTransactionsByRange(request)
                .map(userResponse -> ResponseEntity.status(userResponse.getStatus()).body(userResponse));
    }

    @GetMapping("merchant-get-all")
    public Mono<ResponseEntity<Response<List<Transaction>>>> merchantGetAll(){
        return transactionService.merchantGetAll()
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
