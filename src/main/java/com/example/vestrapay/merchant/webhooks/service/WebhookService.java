package com.example.vestrapay.merchant.webhooks.service;

import com.example.vestrapay.merchant.authentications.interfaces.IAuthenticationService;
import com.example.vestrapay.exceptions.CustomException;
import com.example.vestrapay.utils.dtos.Response;
import com.example.vestrapay.merchant.webhooks.dtos.WebhookDTO;
import com.example.vestrapay.merchant.webhooks.models.Webhook;
import com.example.vestrapay.merchant.webhooks.repository.WebhookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebhookService {
    private final WebhookRepository webhookRepository;
    private final IAuthenticationService authenticationService;

    public Mono<Response<Webhook>> createWebhook(WebhookDTO url) {
        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    log.info("merchant user gotten to create webhook");
                    return webhookRepository.findByMerchantId(user.getMerchantId())
                            .flatMap(webhook -> {
                                log.error("webhook already exist for merchant. updating webhook");
                                webhook.setDateUpdated(LocalDateTime.now());
                                webhook.setUrl(url.getUrl());
                                webhook.setSecretHash(url.getSecretHash());
                                return webhookRepository.save(webhook)
                                        .flatMap(webhook1 -> Mono.just(Response.<Webhook>builder()
                                                .message("SUCCESSFUL")
                                                .data(webhook)
                                                .statusCode(HttpStatus.OK.value())
                                                .status(HttpStatus.OK)
                                                .build()));
                            }).switchIfEmpty(Mono.defer(() -> {
                                return webhookRepository.save(Webhook.builder()
                                                .uuid(UUID.randomUUID().toString())
                                                .url(url.getUrl())
                                                .merchantId(user.getMerchantId())
                                                .secretHash(url.getSecretHash())
                                                .build())
                                        .flatMap(webhook -> {
                                            log.info("webhook successfully created");
                                            return Mono.just(Response.<Webhook>builder()
                                                            .status(HttpStatus.OK)
                                                            .statusCode(HttpStatus.OK.value())
                                                            .message("SUCCESSFUL")
                                                            .data(webhook)
                                                    .build());
                                        }).doOnError(throwable -> {
                                            log.error("error saving webhook. error is {}",throwable.getMessage());
                                            throw new CustomException(throwable);
                                        });
                            }));
                });

    }

    public Mono<Response<Webhook>> viewWebhook() {
        log.info("about viewing webhook for merchant");
        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    log.info("merchant user gotten to create webhook");
                    return webhookRepository.findByMerchantId(user.getMerchantId())
                            .flatMap(webhook -> {
                                log.error("webhook already exist for merchant");
                                return Mono.just(Response.<Webhook>builder()
                                        .message("SUCCESSFUL")
                                        .data(webhook)
                                        .statusCode(HttpStatus.OK.value())
                                        .status(HttpStatus.OK)
                                        .build());
                            }).switchIfEmpty(Mono.defer(() -> {
                                log.error("webhook does not exist for merchant");
                                return Mono.just(Response.<Webhook>builder()
                                                .errors(List.of("Webhook does not exist for merchant"))
                                                .statusCode(HttpStatus.NOT_FOUND.value())
                                                .status(HttpStatus.NOT_FOUND)
                                                .message("FAILED")
                                        .build());
                            }));
                });
    }
}
