package com.example.vestrapay.payment.controller;

import com.example.vestrapay.payment.dtos.PaymentProvidersDTO;
import com.example.vestrapay.payment.dtos.RouteRuleDTO;
import com.example.vestrapay.payment.interfaces.IPaymentMethodInterface;
import com.example.vestrapay.utils.dtos.Response;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/payment-service")
@Tag(name = "PAYMENT SERVICE MANAGEMENT",description = "Payment management service")
@CrossOrigin(origins = "*",maxAge = 3600)
@SecurityRequirement(name = "vestrapay")
@RequiredArgsConstructor
public class PaymentController {
    private final IPaymentMethodInterface paymentMethodInterface;
    @GetMapping("get-all-methods")
    public Mono<ResponseEntity<Response<?>>>getAllPaymentMethods(){
        return paymentMethodInterface.getAllMethods()
                .map(response -> ResponseEntity.status(response.getStatus()).body(response));
    }

    @PostMapping("create-payment-method")
    @PreAuthorize("hasAuthority('CREATE_ADMIN')")
    public Mono<ResponseEntity<Response<?>>>createPaymentMethod(@RequestBody String name){
        return paymentMethodInterface.createPaymentMethod(name)
                .map(response -> ResponseEntity.status(response.getStatus()).body(response));
    }

    @PostMapping("create-payment-provider")
    @PreAuthorize("hasAuthority('CREATE_ADMIN')")
    public Mono<ResponseEntity<Response<?>>>registerProvider(@RequestBody PaymentProvidersDTO request){
        return paymentMethodInterface.registerProvider(request)
                .map(response -> ResponseEntity.status(response.getStatus()).body(response));
    }

    @PostMapping("update-payment-provider")
    @PreAuthorize("hasAuthority('CREATE_ADMIN')")
    public Mono<ResponseEntity<Response<?>>>updateProvider(@RequestBody PaymentProvidersDTO request){
        return paymentMethodInterface.updateProvider(request)
                .map(response -> ResponseEntity.status(response.getStatus()).body(response));
    }

    @GetMapping("view-all-providers")
    @PreAuthorize("hasAuthority('CREATE_ADMIN')")
    public Mono<ResponseEntity<Response<?>>>viewAllProviders(){
        return paymentMethodInterface.viewAllProviders()
                .map(response -> ResponseEntity.status(response.getStatus()).body(response));
    }

    @GetMapping("view-providers-by-method/{method}")
    @PreAuthorize("hasAuthority('CREATE_ADMIN')")
    public Mono<ResponseEntity<Response<?>>>viewProvidersByMethod(@PathVariable("method")String method){
        return paymentMethodInterface.viewAllProvidersByPaymentMethod(method)
                .map(response -> ResponseEntity.status(response.getStatus()).body(response));
    }

    @GetMapping("view-provider-by-method/{uuid}")
    @PreAuthorize("hasAuthority('CREATE_ADMIN')")
    public Mono<ResponseEntity<Response<?>>>viewProviderByUUID(@PathVariable("uuid")String uuid){
        return paymentMethodInterface.viewProviderByUuid(uuid)
                .map(response -> ResponseEntity.status(response.getStatus()).body(response));
    }

    @PostMapping("create-routing-rule")
    @PreAuthorize("hasAuthority('CREATE_ADMIN')")
    public Mono<ResponseEntity<Response<?>>>createRoutingRule(@RequestBody RouteRuleDTO request){
        return paymentMethodInterface.configureRoute(request)
                .map(response -> ResponseEntity.status(response.getStatus()).body(response));
    }

    @PostMapping("update-routing-rule")
    @PreAuthorize("hasAuthority('CREATE_ADMIN')")
    public Mono<ResponseEntity<Response<?>>>updateRoutingRule(@RequestBody RouteRuleDTO request){
        return paymentMethodInterface.updateRoute(request)
                .map(response -> ResponseEntity.status(response.getStatus()).body(response));
    }
}
