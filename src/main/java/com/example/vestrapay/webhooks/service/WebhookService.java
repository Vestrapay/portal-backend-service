package com.example.vestrapay.webhooks.service;

import com.example.vestrapay.utils.dtos.Response;
import com.example.vestrapay.webhooks.dtos.WebhookDTO;
import com.example.vestrapay.webhooks.models.Webhook;
import com.example.vestrapay.webhooks.repository.WebhookRepository;
import io.r2dbc.spi.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebhookService {
    private final WebhookRepository webhookRepository;
    public Mono<Response<Boolean>> processTransaction(Object request){
        return Mono.empty();
    }

    public Mono<Response<Webhook>> createWebhook(WebhookDTO url) {
        return Mono.empty();
    }

    public Mono<Response<Webhook>> viewWebhook() {
        return Mono.empty();
    }
}
