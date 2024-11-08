package com.example.vestrapay.merchant.webhooks.controller;

import com.example.vestrapay.merchant.webhooks.dtos.WebhookDTO;
import com.example.vestrapay.merchant.webhooks.service.WebhookService;
import com.example.vestrapay.utils.dtos.Response;
import com.example.vestrapay.merchant.webhooks.models.Webhook;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/webhook")
@Tag(name = "MERCHANT WEBHOOK CONFIGURATION", description = "Webhook Management for merchants")
@SecurityRequirement(name = "vestrapay")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Validated
public class WebhookController {
    private final WebhookService webhookService;

    @PostMapping("create-webhook")
    public Mono<ResponseEntity<Response<Webhook>>>createWebhook(@RequestBody WebhookDTO request){
        return webhookService.createWebhook(request)
                .map(response -> ResponseEntity.status(response.getStatus()).body(response));
    }

    @GetMapping("view-webhook")
    public Mono<ResponseEntity<Response<Webhook>>>viewWebhook(){
        return webhookService.viewWebhook()
                .map(response -> ResponseEntity.status(response.getStatus()).body(response));
    }
}
