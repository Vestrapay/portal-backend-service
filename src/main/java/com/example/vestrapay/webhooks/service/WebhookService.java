package com.example.vestrapay.webhooks.service;

import com.example.vestrapay.authentications.interfaces.IAuthenticationService;
import com.example.vestrapay.exceptions.CustomException;
import com.example.vestrapay.utils.dtos.Response;
import com.example.vestrapay.webhooks.dtos.WebhookDTO;
import com.example.vestrapay.webhooks.models.Webhook;
import com.example.vestrapay.webhooks.repository.WebhookRepository;
import io.r2dbc.spi.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebhookService {
    private final WebhookRepository webhookRepository;
    private final IAuthenticationService authenticationService;
    public Mono<Response<Boolean>> processTransaction(Object request){
        return Mono.empty();
    }

    public Mono<Response<Webhook>> createWebhook(WebhookDTO url) {
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
