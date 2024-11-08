package com.example.vestrapay.superadmin.charges.service;

import com.example.vestrapay.merchant.authentications.interfaces.IAuthenticationService;
import com.example.vestrapay.merchant.users.enums.UserType;
import com.example.vestrapay.superadmin.charges.dtos.ChargeRequest;
import com.example.vestrapay.superadmin.charges.model.Charge;
import com.example.vestrapay.superadmin.charges.repository.ChargeRepository;
import com.example.vestrapay.utils.dtos.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.example.vestrapay.utils.dtos.Constants.FAILED;
import static com.example.vestrapay.utils.dtos.Constants.SUCCESSFUL;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChargeService {
    public static final String USER_DOES_NOT_HAVE_CREDENTIALS_FOR_THIS_ACTION = "user does not have credentials for this action";
    public static final String NOT_FOUND_OR_LOGGED_IN = "user not found or logged in";
    private final ChargeRepository chargeRepository;
    private final IAuthenticationService authenticationService;
    private final ModelMapper modelMapper;

    public Mono<Response<Charge>> createPaymentCharge(ChargeRequest request) {
        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    if (user.getUserType().equals(UserType.MERCHANT) || user.getUserType().equals(UserType.MERCHANT_USER)){
                        return Mono.just(Response.<Charge>builder()
                                .message(FAILED)
                                .errors(List.of(USER_DOES_NOT_HAVE_CREDENTIALS_FOR_THIS_ACTION))
                                .statusCode(HttpStatus.UNAUTHORIZED.value())
                                .status(HttpStatus.UNAUTHORIZED)
                                .data(null)
                                .build());
                    }
                    Charge paymentCharge = modelMapper.map(request, Charge.class);
                    paymentCharge.setUuid(UUID.randomUUID().toString());
                    return chargeRepository.findByMerchantIdAndPaymentMethodAndCategory(request.getMerchantId(), request.getPaymentMethod(),request.getCategory())
                            .flatMap(charge -> {
                                log.error("charge already exist for merchant with payemnt method and category {}",request);
                                return Mono.just(Response.<Charge>builder()
                                        .data(charge)
                                        .message(FAILED)
                                        .status(HttpStatus.CONFLICT)
                                        .statusCode(HttpStatus.CONFLICT.value())
                                        .build());
                            })
                            .switchIfEmpty(Mono.defer(() -> chargeRepository.save(paymentCharge)
                                    .flatMap(charge -> Mono.just(Response.<Charge>builder()
                                            .data(charge)
                                            .message(SUCCESSFUL)
                                            .status(HttpStatus.CREATED)
                                            .statusCode(HttpStatus.CREATED.value())
                                            .build()))));


                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error(NOT_FOUND_OR_LOGGED_IN);
                    return Mono.just(Response.<Charge>builder()
                                    .message(FAILED)
                                    .errors(List.of(NOT_FOUND_OR_LOGGED_IN))
                                    .statusCode(HttpStatus.NOT_FOUND.value())
                                    .status(HttpStatus.NOT_FOUND)
                                    .data(null)
                            .build());
                }));
    }

    public Mono<Response<Charge>> updatePaymentCharge(ChargeRequest request) {
        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    if (user.getUserType().equals(UserType.MERCHANT) || user.getUserType().equals(UserType.MERCHANT_USER)){
                        return Mono.just(Response.<Charge>builder()
                                .message(FAILED)
                                .errors(List.of(USER_DOES_NOT_HAVE_CREDENTIALS_FOR_THIS_ACTION))
                                .statusCode(HttpStatus.UNAUTHORIZED.value())
                                .status(HttpStatus.UNAUTHORIZED)
                                .data(null)
                                .build());
                    }
                    return chargeRepository.findByMerchantIdAndPaymentMethodAndCategory(request.getMerchantId(), request.getPaymentMethod(),request.getCategory())
                            .flatMap(charge -> {
                                modelMapper.map(request,charge);
                                return chargeRepository.save(charge).flatMap(updatedCharge -> Mono.just(Response.<Charge>builder()
                                        .data(updatedCharge)
                                        .message(SUCCESSFUL)
                                        .status(HttpStatus.OK)
                                        .statusCode(HttpStatus.OK.value())
                                        .build()));
                            })
                            .switchIfEmpty(Mono.defer(() -> Mono.just(Response.<Charge>builder()
                                    .message(FAILED)
                                    .errors(List.of("payment charge not found"))
                                    .status(HttpStatus.NOT_FOUND)
                                    .statusCode(HttpStatus.NOT_FOUND.value())
                                    .build())));


                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error(NOT_FOUND_OR_LOGGED_IN);
                    return Mono.just(Response.<Charge>builder()
                            .message(FAILED)
                            .errors(List.of(NOT_FOUND_OR_LOGGED_IN))
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .status(HttpStatus.NOT_FOUND)
                            .data(null)
                            .build());
                }));    }

    public Mono<Response<List<Charge>>> viewMerchantCharge(String merchantId) {
        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    if (user.getUserType().equals(UserType.MERCHANT) || user.getUserType().equals(UserType.MERCHANT_USER)){
                        return Mono.just(Response.<List<Charge>>builder()
                                .message(FAILED)
                                .errors(List.of(USER_DOES_NOT_HAVE_CREDENTIALS_FOR_THIS_ACTION))
                                .statusCode(HttpStatus.UNAUTHORIZED.value())
                                .status(HttpStatus.UNAUTHORIZED)
                                .data(null)
                                .build());
                    }
                    return chargeRepository.findAllByMerchantId(merchantId)
                            .collectList()
                            .flatMap(charges -> Mono.just(Response.<List<Charge>>builder()
                                            .data(charges)
                                            .message(SUCCESSFUL)
                                            .statusCode(HttpStatus.OK.value())
                                            .status(HttpStatus.OK)
                                    .build())).switchIfEmpty(Mono.defer(() -> Mono.just(Response.<List<Charge>>builder()
                                    .data(new ArrayList<>())
                                    .message(SUCCESSFUL)
                                    .statusCode(HttpStatus.OK.value())
                                    .status(HttpStatus.OK)
                                    .build())));


                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error(NOT_FOUND_OR_LOGGED_IN);
                    return Mono.just(Response.<List<Charge>>builder()
                            .message(FAILED)
                            .errors(List.of(NOT_FOUND_OR_LOGGED_IN))
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .status(HttpStatus.NOT_FOUND)
                            .data(null)
                            .build());
                }));
    }

    public Mono<Response<List<Charge>>> viewAllCharges() {
        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    if (user.getUserType().equals(UserType.MERCHANT) || user.getUserType().equals(UserType.MERCHANT_USER)){
                        return Mono.just(Response.<List<Charge>>builder()
                                .message(FAILED)
                                .errors(List.of(USER_DOES_NOT_HAVE_CREDENTIALS_FOR_THIS_ACTION))
                                .statusCode(HttpStatus.UNAUTHORIZED.value())
                                .status(HttpStatus.UNAUTHORIZED)
                                .data(null)
                                .build());
                    }
                    return chargeRepository.findAll()
                            .collectList()
                            .flatMap(charges -> Mono.just(Response.<List<Charge>>builder()
                                    .data(charges)
                                    .message(SUCCESSFUL)
                                    .statusCode(HttpStatus.OK.value())
                                    .status(HttpStatus.OK)
                                    .build()))
                            .switchIfEmpty(Mono.defer(() -> Mono.just(Response.<List<Charge>>builder()
                                    .data(new ArrayList<>())
                                    .message(SUCCESSFUL)
                                    .statusCode(HttpStatus.OK.value())
                                    .status(HttpStatus.OK)
                                    .build())));
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error(NOT_FOUND_OR_LOGGED_IN);
                    return Mono.just(Response.<List<Charge>>builder()
                            .message(FAILED)
                            .errors(List.of(NOT_FOUND_OR_LOGGED_IN))
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .status(HttpStatus.NOT_FOUND)
                            .data(null)
                            .build());
                }));
    }

    public Mono<Response<Void>> deleteCharge(String chargeId) {
        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    if (user.getUserType().equals(UserType.MERCHANT) || user.getUserType().equals(UserType.MERCHANT_USER)){
                        return Mono.just(Response.<Void>builder()
                                .message(FAILED)
                                .errors(List.of(USER_DOES_NOT_HAVE_CREDENTIALS_FOR_THIS_ACTION))
                                .statusCode(HttpStatus.UNAUTHORIZED.value())
                                .status(HttpStatus.UNAUTHORIZED)
                                .build());
                    }
                    return chargeRepository.findByUuid(chargeId)
                            .flatMap(charge -> {
                                chargeRepository.delete(charge).subscribe();
                                return Mono.just(Response.<Void>builder()
                                        .status(HttpStatus.NO_CONTENT)
                                        .statusCode(HttpStatus.NO_CONTENT.value())
                                        .message(SUCCESSFUL)
                                        .build());

                            }).switchIfEmpty(Mono.defer(() -> Mono.just(Response.<Void>builder()
                                            .status(HttpStatus.NOT_FOUND)
                                            .statusCode(HttpStatus.NOT_FOUND.value())
                                            .errors(List.of("Charge not found"))
                                            .message(FAILED)
                                    .build())));

                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error(NOT_FOUND_OR_LOGGED_IN);
                    return Mono.just(Response.<Void>builder()
                            .message(FAILED)
                            .errors(List.of(NOT_FOUND_OR_LOGGED_IN))
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .status(HttpStatus.NOT_FOUND)
                            .data(null)
                            .build());
                }));

    }
}
