package com.example.vestrapay.webhooks.repository;

import com.example.vestrapay.webhooks.models.Webhook;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface WebhookRepository extends R2dbcRepository<Webhook,Long> {
}
