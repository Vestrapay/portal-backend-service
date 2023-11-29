package com.example.vestrapay.dispute.service;

import com.example.vestrapay.authentications.interfaces.IAuthenticationService;
import com.example.vestrapay.dispute.dto.DisputeDTO;
import com.example.vestrapay.dispute.entity.Dispute;
import com.example.vestrapay.dispute.enums.DisputeEnum;
import com.example.vestrapay.dispute.interfaces.IDisputeService;
import com.example.vestrapay.dispute.repository.DisputeRepository;
import com.example.vestrapay.notifications.interfaces.INotificationService;
import com.example.vestrapay.transactions.reporitory.TransactionRepository;
import com.example.vestrapay.utils.dtos.Response;
import com.example.vestrapay.utils.file_upload.IFileServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DisputeService implements IDisputeService {
    public static final String FAILED = "Failed";
    public static final String SUCCESSFUL = "Successful";
    private final DisputeRepository disputeRepository;
    private final IAuthenticationService authenticationService;
    private final INotificationService notificationService;
    private final IFileServiceImpl fileService;
    private final TransactionRepository transactionRepository;

    @Override
    public Mono<Response<Object>> logDispute(DisputeDTO request, MultipartFile[] file) {
        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    Dispute dispute = Dispute.builder()
                            .comment(request.getComment())
                            .merchantId(user.getMerchantId())
                            .uuid(UUID.randomUUID().toString())
                            .transactionReference(request.getTransactionReference())
                            .status(DisputeEnum.OPENED)
                            .build();
                    return transactionRepository.findByMerchantIdAndTransactionReference(user.getMerchantId(), dispute.getTransactionReference())
                            .flatMap(transaction -> {
                                if (file==null){
                                    return disputeRepository.save(dispute)
                                            .flatMap(dispute1 -> {
                                                log.info("dispute logged. {}",dispute1.toString());
                                                notificationService.notifyAdmins("dispute logged for transaction reference "+
                                                        request.getTransactionReference()+"\n user is "+user.getEmail()+"\n"
                                                        +request.getComment()).subscribe();
                                                return Mono.just(Response.builder()
                                                        .statusCode(HttpStatus.OK.value())
                                                        .status(HttpStatus.OK)
                                                        .message("SUCCESSFUL")
                                                        .data(dispute1)
                                                        .build());
                                            });
                                }
                                return fileService.singleUpload(user.getMerchantId(), file)
                                        .flatMap(s -> {
                                            dispute.setFileUrl(s.toString());
                                            return disputeRepository.save(dispute)
                                                    .flatMap(dispute1 -> {
                                                        log.info("dispute logged. {}",dispute1.toString());
                                                        notificationService.notifyAdmins("dispute logged for transaction reference "+
                                                                request.getTransactionReference()+"\n user is "+user.getEmail()+"\n"
                                                                +request.getComment()).subscribe();
                                                        return Mono.just(Response.builder()
                                                                .statusCode(HttpStatus.OK.value())
                                                                .status(HttpStatus.OK)
                                                                .message("SUCCESSFUL")
                                                                .data(dispute1)
                                                                .build());
                                                    });
                                        });

                            })
                            .switchIfEmpty(Mono.defer(() -> {
                                log.error("transaction not found to dispute");
                                return Mono.just(Response.builder()
                                        .statusCode(HttpStatus.NOT_FOUND.value())
                                        .status(HttpStatus.NOT_FOUND)
                                        .message(SUCCESSFUL)
                                                .errors(List.of("Transaction not found with reference "+dispute.getTransactionReference()))
                                        .build());
                            }));

                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("user not logged in");
                    return Mono.just(Response.builder()
                                    .message(FAILED)
                                    .errors(List.of("user not logged in"))
                                    .status(HttpStatus.UNAUTHORIZED)
                                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                            .build());
                }));
    }

    @Override
    public Mono<Response<Object>> viewDisputeById(String uuid) {
        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    return disputeRepository.findByMerchantIdAndUuid(user.getMerchantId(),uuid)
                            .flatMap(dispute -> {
                                return Mono.just(Response.builder()
                                                .message(SUCCESSFUL)
                                                .statusCode(HttpStatus.OK.value())
                                                .status(HttpStatus.OK)
                                                .data(dispute)
                                        .build());

                            }).switchIfEmpty(Mono.defer(() -> {
                                log.error("dispute not found with UUID {}",uuid);
                                return Mono.just(Response.builder()
                                                .errors(List.of("dispute not found with UUID"))
                                                .statusCode(HttpStatus.NOT_FOUND.value())
                                                .status(HttpStatus.NOT_FOUND)
                                                .message(FAILED)
                                        .build());
                            }));

                }).switchIfEmpty(Mono.defer(() -> {
                    log.error("user not logged in");
                    return Mono.just(Response.builder()
                            .message("Failed")
                            .errors(List.of("user not logged in"))
                            .status(HttpStatus.UNAUTHORIZED)
                            .statusCode(HttpStatus.UNAUTHORIZED.value())
                            .build());
                })).cache(Duration.ofMinutes(2));
    }

    @Override
    public Mono<Response<Object>> viewAllDispute() {
        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    return disputeRepository.findByMerchantId(user.getMerchantId())
                            .collectList()
                            .flatMap(dispute -> {
                                return Mono.just(Response.builder()
                                        .message(SUCCESSFUL)
                                        .statusCode(HttpStatus.OK.value())
                                        .status(HttpStatus.OK)
                                        .data(dispute)
                                        .build());

                            }).switchIfEmpty(Mono.defer(() -> {
                                return Mono.just(Response.builder()
                                        .errors(List.of("dispute not found"))
                                        .statusCode(HttpStatus.NOT_FOUND.value())
                                        .status(HttpStatus.NOT_FOUND)
                                        .message(FAILED)
                                        .build());
                            }));

                }).switchIfEmpty(Mono.defer(() -> {
                    log.error("user not logged in");
                    return Mono.just(Response.builder()
                            .message("Failed")
                            .errors(List.of("user not logged in"))
                            .status(HttpStatus.UNAUTHORIZED)
                            .statusCode(HttpStatus.UNAUTHORIZED.value())
                            .build());
                })).cache(Duration.ofMinutes(2));
    }

    @Override
    public Mono<Response<Object>> viewByParam(Map<String, String> param) {
        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    Dispute dispute = new Dispute();
                    for (String key: param.keySet()) {
                        if (key.equalsIgnoreCase("reference"))
                            dispute.setTransactionReference(param.get(key));
                        if (key.equalsIgnoreCase("merchantId"))
                            dispute.setMerchantId(param.get(key));
                    }
                    return disputeRepository.findAll(Example.of(dispute, ExampleMatcher.matchingAny()), Sort.by("id").descending())
                            .collectList()
                            .flatMap(disputes -> {
                                return Mono.just(Response.builder()
                                                .data(disputes)
                                                .status(HttpStatus.OK)
                                                .statusCode(HttpStatus.OK.value())
                                                .message(SUCCESSFUL)
                                        .build());

                            }).switchIfEmpty(Mono.defer(() -> {
                                return Mono.just(Response.builder()
                                        .errors(List.of("dispute not found"))
                                        .statusCode(HttpStatus.NOT_FOUND.value())
                                        .status(HttpStatus.NOT_FOUND)
                                        .message(FAILED)
                                        .build());
                            }));

                }).switchIfEmpty(Mono.defer(() -> {
                    log.error("user not logged in");
                    return Mono.just(Response.builder()
                            .message("Failed")
                            .errors(List.of("user not logged in"))
                            .status(HttpStatus.UNAUTHORIZED)
                            .statusCode(HttpStatus.UNAUTHORIZED.value())
                            .build());
                }));
    }
}
