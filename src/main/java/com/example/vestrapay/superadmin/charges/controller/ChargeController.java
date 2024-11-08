package com.example.vestrapay.superadmin.charges.controller;

import com.example.vestrapay.superadmin.charges.dtos.ChargeRequest;
import com.example.vestrapay.superadmin.charges.service.ChargeService;
import com.example.vestrapay.utils.dtos.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/charges")
@Tag(name = "CHARGES", description = "Charge Management")
@SecurityRequirement(name = "vestrapay")
@CrossOrigin(origins ="*",maxAge = 3600)
@RequiredArgsConstructor
public class ChargeController {
    private final ChargeService chargeService;
    @PostMapping("create-charge")
    @Operation(description = "this is used for creating a charge per payment method")
    public Mono<ResponseEntity<Response<?>>> createPaymentCharge(@RequestBody ChargeRequest request){
        return chargeService.createPaymentCharge(request)
                .map(chargeResponse -> ResponseEntity.status(chargeResponse.getStatus()).body(chargeResponse));
    }

    @PostMapping("update-charge")
    public Mono<ResponseEntity<Response<?>>> updatePaymentCharge(@RequestBody ChargeRequest request){
        return chargeService.updatePaymentCharge(request)
                .map(chargeResponse -> ResponseEntity.status(chargeResponse.getStatus()).body(chargeResponse));
    }

    @GetMapping("view-charge/{merchantId}")
    public Mono<ResponseEntity<Response<?>>> viewMerchantCharge(@PathVariable("merchantId") String merchantId){
        return chargeService.viewMerchantCharge(merchantId)
                .map(chargeResponse -> ResponseEntity.status(chargeResponse.getStatus()).body(chargeResponse));
    }

    @GetMapping("view-charge")
    public Mono<ResponseEntity<Response<?>>> viewAllCharges(){
        return chargeService.viewAllCharges()
                .map(chargeResponse -> ResponseEntity.status(chargeResponse.getStatus()).body(chargeResponse));
    }

    @DeleteMapping("delete-charge/{chargeId}")
    public Mono<ResponseEntity<Response<Void>>> viewAllCharges(@PathVariable("chargeId")String chargeId){
        return chargeService.deleteCharge(chargeId)
                .map(chargeResponse -> ResponseEntity.status(chargeResponse.getStatus()).body(chargeResponse));
    }
}
