package com.example.vestrapay.superadmin.dispute.services;

import com.example.vestrapay.exceptions.CustomException;
import com.example.vestrapay.merchant.authentications.interfaces.IAuthenticationService;
import com.example.vestrapay.merchant.dispute.enums.DisputeEnum;
import com.example.vestrapay.merchant.dispute.interfaces.IDisputeFileService;
import com.example.vestrapay.merchant.dispute.repository.DisputeRepository;
import com.example.vestrapay.merchant.notifications.interfaces.INotificationService;
import com.example.vestrapay.merchant.notifications.models.EmailDTO;
import com.example.vestrapay.merchant.users.repository.UserRepository;
import com.example.vestrapay.superadmin.dispute.dtos.UpdateDisputeDTO;
import com.example.vestrapay.superadmin.dispute.interfaces.IAdminDisputeService;
import com.example.vestrapay.utils.dtos.Response;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.example.vestrapay.utils.dtos.Constants.FAILED;
import static com.example.vestrapay.utils.dtos.Constants.SUCCESSFUL;

@Service
@AllArgsConstructor
@Slf4j
public class AdminDisputeService implements IAdminDisputeService {
    private final IDisputeFileService disputeFileService;
    private final DisputeRepository disputeRepository;
    private final INotificationService notificationService;
    private final UserRepository userRepository;
    private final IAuthenticationService authenticationService;


    @Override
    public Mono<Response<Object>> viewAll() {
        return disputeRepository.findAll()
                .collectList()
                .flatMap(disputes -> Mono.just(Response.builder()
                        .data(disputes)
                        .statusCode(HttpStatus.OK.value())
                        .status(HttpStatus.OK)
                        .message(SUCCESSFUL)
                        .build()))
                .switchIfEmpty(Mono.defer(() -> Mono.just(Response.builder()
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .status(HttpStatus.NOT_FOUND)
                        .message(FAILED)
                        .errors(List.of("disputes not found"))
                        .build())))
                .doOnError(throwable -> {
                    log.error("error fetching all disputes {}",throwable.getMessage());
                    throw new CustomException(Response.builder()
                            .message(FAILED)
                            .errors(List.of(throwable.getMessage(), throwable.getLocalizedMessage()))
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @Override
    public Mono<Response<Object>> viewAllPending() {
        return disputeRepository.findByStatusOrStatus(DisputeEnum.OPENED,DisputeEnum.PENDING)
                .collectList()
                .flatMap(disputes -> Mono.just(Response.builder()
                                .data(disputes)
                                .statusCode(HttpStatus.OK.value())
                                .status(HttpStatus.OK)
                                .message(SUCCESSFUL)
                        .build()))
                .switchIfEmpty(Mono.defer(() -> Mono.just(Response.builder()
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .status(HttpStatus.NOT_FOUND)
                                .message(FAILED)
                                .errors(List.of("disputes not found"))
                        .build())))
                .doOnError(throwable -> {
                    log.error("error fetching all pending disputes {}",throwable.getMessage());
                    throw new CustomException(Response.builder()
                            .message("FAILED")
                            .errors(List.of(throwable.getMessage(), throwable.getLocalizedMessage()))
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @Override
    public Mono<Response<Object>> findDisputeById(String disputeId) {
        return disputeRepository.findByUuid(disputeId)
                .flatMap(dispute -> Mono.just(Response.builder()
                        .data(dispute)
                        .statusCode(HttpStatus.OK.value())
                        .status(HttpStatus.OK)
                        .message(SUCCESSFUL)
                        .build()))
                .switchIfEmpty(Mono.defer(() -> Mono.just(Response.builder()
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .status(HttpStatus.NOT_FOUND)
                        .message(FAILED)
                        .errors(List.of("dispute not found"))
                        .build())))
                .doOnError(throwable -> {
                    log.error("error dispute, error is {}",throwable.getMessage());
                    throw new CustomException(Response.builder()
                            .message(FAILED)
                            .errors(List.of(throwable.getMessage(), throwable.getLocalizedMessage()))
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });    }

    @Override
    public Mono<Response<Object>> getDisputeDocuments(String transactionReference) {
        Response<Object> response = Response.builder()
                .message(SUCCESSFUL)
                .data(disputeFileService.loadAll(transactionReference))
                .statusCode(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .build();

        return Mono.just(response);    }

    @Override
    public Mono<Response<Object>> updateDispute(UpdateDisputeDTO request) {
        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    return disputeRepository.findByMerchantIdAndUuid(request.getMerchantId(),request.getDisputeId())
                            .flatMap(dispute -> {
                                dispute.setStatus(request.getStatus());
                                if (!request.getReason().isEmpty())
                                    dispute.setComment(request.getReason());

                                return disputeRepository.save(dispute)
                                        .flatMap(dispute1 -> {
                                            return userRepository.findByMerchantIdAndUuid(request.getMerchantId(), request.getMerchantId())
                                                    .flatMap(merchant -> {

                                                        CompletableFuture.runAsync(() -> notificationService.sendEmailAsync(EmailDTO.builder()
                                                                .to(merchant.getEmail())
                                                                .subject("DISPUTE STATUS UPDATED FOR TRANSACTION "+dispute1.getTransactionReference())
                                                                .body("TRANSACTION DISPUTE "+dispute1.getTransactionReference()+" status updated to "+request.getStatus())
                                                                .build()).subscribe());

                                                        log.info("dispute {} with reference {} updated to {} by {}",request.getDisputeId(),dispute.getTransactionReference(),request.getStatus(),user.getEmail());
                                                        return Mono.just(Response.builder()
                                                                        .status(HttpStatus.OK)
                                                                        .statusCode(HttpStatus.OK.value())
                                                                        .message(SUCCESSFUL)
                                                                        .data(dispute1)
                                                                .build());

                                                    })
                                                    .switchIfEmpty(Mono.defer(() -> {
                                                        log.error("merchant not found for logged dispute");
                                                        return Mono.just(Response.builder()
                                                                .statusCode(HttpStatus.NOT_FOUND.value())
                                                                .status(HttpStatus.NOT_FOUND)
                                                                .message(FAILED)
                                                                .errors(List.of("merchant not found for logged dispute"))
                                                                .build());
                                                    }));
                                        })
                                        .doOnError(throwable -> {
                                            log.error("error updating dispute, error is {}",throwable.getMessage());
                                            throw new CustomException(Response.builder()
                                                    .message(FAILED)
                                                    .errors(List.of(throwable.getMessage(), throwable.getLocalizedMessage(),"error updating dispute"))
                                                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                                    .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                                        });

                            }).switchIfEmpty(Mono.defer(() -> Mono.just(Response.builder()
                                    .statusCode(HttpStatus.NOT_FOUND.value())
                                    .status(HttpStatus.NOT_FOUND)
                                    .message(FAILED)
                                    .errors(List.of("dispute not found"))
                                    .build())))
                            .doOnError(throwable -> {
                                log.error("error updating dispute, error is {}",throwable.getMessage());
                                throw new CustomException(Response.builder()
                                        .message(FAILED)
                                        .errors(List.of(throwable.getMessage(), throwable.getLocalizedMessage(),"error updating dispute"))
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                            });
                });

    }
}
