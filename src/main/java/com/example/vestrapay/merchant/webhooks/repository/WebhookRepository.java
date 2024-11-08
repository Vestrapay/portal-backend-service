package com.example.vestrapay.merchant.webhooks.repository;

import com.example.vestrapay.merchant.webhooks.models.Webhook;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface WebhookRepository extends R2dbcRepository<Webhook,Long> {
    Mono<Webhook>findByMerchantId(String merchantId);
}
