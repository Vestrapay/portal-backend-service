package com.example.vestrapay.settlements.controllers;

import com.example.vestrapay.settlements.dtos.SettlementDTO;
import com.example.vestrapay.settlements.interfaces.ISettlementService;
import com.example.vestrapay.settlements.models.Settlement;
import com.example.vestrapay.settlements.models.WemaAccounts;
import com.example.vestrapay.users.models.User;
import com.example.vestrapay.utils.dtos.Response;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("api/v1/settlement")
@Tag(name = "SETTLEMENT", description = "Settlement Management")
@SecurityRequirement(name = "vestrapay")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Validated
public class SettlementController {
    private final ISettlementService settlementService;

    @PostMapping("create")
    public Mono<ResponseEntity<Response<Settlement>>>createAccount(@RequestBody SettlementDTO request){
        return settlementService.addAccount(request)
                .map(settlementResponse -> ResponseEntity.status(settlementResponse.getStatus()).body(settlementResponse));
    }

    @PutMapping("update")
    public Mono<ResponseEntity<Response<Settlement>>>update(@RequestBody Settlement request){
        return settlementService.updateAccount(request)
                .map(settlementResponse -> ResponseEntity.status(settlementResponse.getStatus()).body(settlementResponse));
    }

    @DeleteMapping("remove")
    public Mono<ResponseEntity<Response<Void>>>remove(@RequestBody Settlement request){
        return settlementService.removeAccount(request)
                .map(settlementResponse -> ResponseEntity.status(settlementResponse.getStatus()).body(settlementResponse));
    }

    @PostMapping("primary")
    public Mono<ResponseEntity<Response<Void>>>setPrimaryAccount(@RequestBody String request){
        return settlementService.setPrimaryAccount(request)
                .map(settlementResponse -> ResponseEntity.status(settlementResponse.getStatus()).body(settlementResponse));
    }

    @GetMapping("view-primary-account")
    public Mono<ResponseEntity<Response<Settlement>>>viewPrimaryAccount(){
        return settlementService.viewPrimaryAccount()
                .map(settlementResponse -> ResponseEntity.status(settlementResponse.getStatus()).body(settlementResponse));
    }

    @GetMapping("view-account/{uuid}")
    public Mono<ResponseEntity<Response<Settlement>>>viewAccount(@PathVariable("uuid")String uuid){
        return settlementService.viewAccount(uuid)
                .map(settlementResponse -> ResponseEntity.status(settlementResponse.getStatus()).body(settlementResponse));
    }

    @GetMapping("view-all")
    public Mono<ResponseEntity<Response<List<Settlement>>>>viewAllAccounts(){
        return settlementService.viewAllUserAccounts()
                .map(settlementResponse -> ResponseEntity.status(settlementResponse.getStatus()).body(settlementResponse));
    }

    @GetMapping("view-wema-account")
    public Mono<ResponseEntity<Response<WemaAccounts>>>viewMerchantWemaAccount(){
        return settlementService.viewWemaAccount()
                .map(settlementResponse -> ResponseEntity.status(settlementResponse.getStatus()).body(settlementResponse));
    }

    @PostMapping("merchant-generate-wema-account")
    public Mono<ResponseEntity<Response<WemaAccounts>>>viewMerchantWemaAccount(@RequestBody User request){
        return settlementService.generateWemaAccountForMerchant(request)
                .map(settlementResponse -> ResponseEntity.status(settlementResponse.getStatus()).body(settlementResponse));
    }
}
