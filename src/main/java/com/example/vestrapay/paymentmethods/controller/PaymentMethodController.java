package com.example.vestrapay.paymentmethods.controller;

import com.example.vestrapay.paymentmethods.interfaces.IPaymentMethodInterface;
import com.example.vestrapay.utils.dtos.Response;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/payment-method")
@Tag(name = "PAYMENT METHOD MANAGEMENT",description = "Payment method management service")
@CrossOrigin(origins = "*",maxAge = 3600)
@SecurityRequirement(name = "vestrapay")
@RequiredArgsConstructor
public class PaymentMethodController {
    private final IPaymentMethodInterface paymentMethodInterface;
    @GetMapping("get-all-methods")
    public Mono<ResponseEntity<Response<?>>>getAllPaymentMethods(){
        return paymentMethodInterface.getAllMethods()
                .map(response -> ResponseEntity.status(response.getStatus()).body(response));
    }
}
