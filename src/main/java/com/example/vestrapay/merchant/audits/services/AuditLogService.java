package com.example.vestrapay.merchant.audits.services;

import com.example.vestrapay.merchant.audits.models.AuditEvent;
import com.example.vestrapay.merchant.audits.repository.AuditLogRepository;
import com.example.vestrapay.merchant.authentications.interfaces.IAuthenticationService;
import com.example.vestrapay.exceptions.CustomException;
import com.example.vestrapay.utils.dtos.PagedRequest;
import com.example.vestrapay.utils.dtos.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.example.vestrapay.utils.dtos.Constants.FAILED;
import static com.example.vestrapay.utils.dtos.Constants.SUCCESSFUL;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final IAuthenticationService authenticationService;

    @Async
    public void log(AuditEvent event) {
        log.info("logging event {}", event.toString());
        auditLogRepository.save(event).subscribe();
    }

    public Mono<Response<List<AuditEvent>>> getLogs(PagedRequest request) {
        return authenticationService.getLoggedInUser().flatMap(user -> {
                    return auditLogRepository.getLogs(request.getFrom(), request.getTo(), request.getSortBy()).collectList()
                            .flatMap(auditEvents -> {
                                log.info("logs gotten {}", auditEvents);
                                return Mono.just(Response.<List<AuditEvent>>builder()
                                        .message(SUCCESSFUL)
                                        .data(auditEvents)
                                        .status(HttpStatus.OK)
                                        .statusCode(HttpStatus.OK.value())
                                        .build());
                            }).switchIfEmpty(Mono.defer(() -> {
                                log.error("no logs found for {}",user.getEmail());
                                return Mono.just(Response.<List<AuditEvent>>builder()
                                        .message(FAILED)
                                        .status(HttpStatus.NOT_FOUND)
                                        .statusCode(HttpStatus.NOT_FOUND.value())
                                        .errors(List.of("logs not found"))
                                        .build());
                            })).doOnError(throwable -> {
                                log.error("error fetching logs from db. error is {}",throwable.getLocalizedMessage());
                                throw new CustomException(Response.builder()
                                        .errors(List.of("error getting logs from db", throwable.getMessage(), throwable.getLocalizedMessage()))
                                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .message(FAILED)
                                        .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                            });
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("user not found or logged in");
                    return Mono.just(Response.<List<AuditEvent>>builder()
                            .message(FAILED)
                            .status(HttpStatus.NOT_FOUND)
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .errors(List.of("user not found or logged in"))
                            .build());
                })).doOnError(throwable -> {
                    log.error("error getting logged in user error is {}", throwable.getLocalizedMessage());
                    throw new CustomException(Response.builder()
                            .errors(List.of("error getting logged in user", throwable.getMessage(), throwable.getLocalizedMessage()))
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message(FAILED)
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });


    }
}
