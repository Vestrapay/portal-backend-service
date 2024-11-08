package com.example.vestrapay.merchant.business.services;

import com.example.vestrapay.merchant.authentications.services.AuthenticationService;
import com.example.vestrapay.merchant.business.dtos.BusinessDTO;
import com.example.vestrapay.merchant.business.interfaces.IBusinessService;
import com.example.vestrapay.merchant.business.models.Business;
import com.example.vestrapay.merchant.business.repository.BusinessRepository;
import com.example.vestrapay.exceptions.CustomException;
import com.example.vestrapay.utils.dtos.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static com.example.vestrapay.utils.dtos.Constants.FAILED;
import static com.example.vestrapay.utils.dtos.Constants.SUCCESSFUL;

@Service
@Slf4j
@RequiredArgsConstructor
public class BusinessService implements IBusinessService {
    private final BusinessRepository businessRepository;
    private final AuthenticationService authenticationService;
    @Override
    public Mono<Response<Business>> register(BusinessDTO request) {
        log.info("register business for merchant with DTO {}",request.toString());

        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    log.info("about registering business for merchant {} with DTO {}",user.getMerchantId(),request.toString());
                    //todo in the future this implementation can be changed to handle multiple businesses for a merchant.
                    return businessRepository.findBusinessByMerchantIdOrBusinessEmailOrBusinessSupportEmailAddressOrBusinessSupportPhoneNumber(user.getMerchantId(),
                                    request.getBusinessEmail(),request.getBusinessSupportEmailAddress(),request.getBusinessSupportPhoneNumber())
                            .flatMap(business -> {
                                log.error("business already exist for merchant {}",user.getFirstName());
                                return Mono.just(Response.<Business>builder()
                                                .status(HttpStatus.CONFLICT)
                                                .statusCode(HttpStatus.CONFLICT.value())
                                                .message(FAILED)
                                                .errors(List.of("Business already Exists"))
                                        .build());
                            })
                            .switchIfEmpty(Mono.defer(() -> {
                                Business business = Business.builder()
                                        .businessEmail(request.getBusinessEmail())
                                        .businessAddress(request.getBusinessAddress())
                                        .businessName(request.getBusinessName())
                                        .businessPhoneNumber(request.getBusinessPhoneNumber())
                                        .businessSupportPhoneNumber(request.getBusinessSupportPhoneNumber())
                                        .chargeBackEmail(request.getChargeBackEmail())
                                        .country(request.getCountry())
                                        .businessSupportEmailAddress(request.getBusinessSupportEmailAddress())
                                        .customerPayTransactionFee(request.isCustomerPayTransactionFee())
                                        .merchantId(user.getMerchantId())
                                        .emailNotification(request.isEmailNotification())
                                        .customerNotification(request.isCustomerNotification())
                                        .creditNotifications(request.isCreditNotifications())
                                        .notifyOnlyBusinessEmail(request.isNotifyOnlyBusinessEmail())
                                        .notifyDashboardUsers(request.isNotifyDashboardUsers())
                                        .sendToSpecificUsers(request.getSendToSpecificUsers())
                                        .twoFAlogin(request.isTwoFAlogin())
                                        .twoFAForTransfer(request.isTwoFAForTransfer())
                                        .transfersViaAPI(request.isTransfersViaAPI())
                                        .transfersViaDashboard(request.isTransfersViaDashboard())
                                        .disableAllTransfers(request.isDisableAllTransfers())
                                        .paymentMethod(request.getPaymentMethod())
                                        .uuid(UUID.randomUUID().toString()).build();

                                return businessRepository.save(business)
                                        .flatMap(business1 -> {
                                            log.info("business successfully registered");
                                            return Mono.just(Response.<Business>builder()
                                                            .data(business1)
                                                            .message(SUCCESSFUL)
                                                            .statusCode(HttpStatus.CREATED.value())
                                                            .status(HttpStatus.CREATED)
                                                    .build());
                                        }).doOnError(throwable -> {
                                            log.error("error saving business, error is {}",throwable.getLocalizedMessage());
                                            throw new CustomException(Response.<Business>builder()
                                                    .message(FAILED)
                                                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                    .errors(List.of("error saving business",throwable.getLocalizedMessage(),throwable.getMessage()))
                                                    .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                                        });
                            }));
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("user not logged in or exist");
                    return Mono.just(Response.<Business>builder()
                            .status(HttpStatus.NOT_FOUND)
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .message(FAILED)
                            .errors(List.of("user not logged in or found"))
                            .build());
                })).doOnError(throwable -> {
                    log.error("error fetching logged in user, error is {}", throwable.getLocalizedMessage());
                    throw new CustomException(Response.<Void>builder()
                            .message(FAILED)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .errors(List.of("error fetching logged in user",throwable.getLocalizedMessage(),throwable.getMessage()))
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @Override
    public Mono<Response<Business>> update(BusinessDTO request) {
        log.info("about updating business with DTO {}",request.toString());
        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    log.info("about updating business for merchant {} with DTO {}",user.getMerchantId(),request.toString());
                    return businessRepository.findBusinessByMerchantId(user.getMerchantId())
                            .flatMap(business -> {
                                business.setBusinessName(request.getBusinessName());
                                business.setBusinessEmail(request.getBusinessEmail());
                                business.setBusinessAddress(request.getBusinessAddress());
                                business.setBusinessPhoneNumber(request.getBusinessPhoneNumber());
                                business.setCountry(request.getCountry());
                                business.setBusinessSupportEmailAddress(request.getBusinessSupportEmailAddress());
                                business.setBusinessSupportPhoneNumber(request.getBusinessSupportPhoneNumber());
                                business.setCustomerNotification(request.isCustomerNotification());
                                business.setEmailNotification(request.isEmailNotification());
                                business.setCustomerNotification(request.isCustomerNotification());
                                business.setCreditNotifications(request.isCreditNotifications());
                                business.setNotifyOnlyBusinessEmail(request.isNotifyOnlyBusinessEmail());
                                business.setNotifyDashboardUsers(request.isNotifyDashboardUsers());
                                business.setSendToSpecificUsers(request.getSendToSpecificUsers());
                                business.setTwoFAlogin(request.isTwoFAlogin());
                                business.setTwoFAForTransfer(request.isTwoFAForTransfer());
                                business.setTransfersViaAPI(request.isTransfersViaAPI());
                                business.setTransfersViaDashboard(request.isTransfersViaDashboard());
                                business.setDisableAllTransfers(request.isDisableAllTransfers());
                                business.setPaymentMethod(request.getPaymentMethod());
                                business.setSettlementTime(request.getSettlementTime());

                                return businessRepository.save(business)
                                        .flatMap(business1 -> {
                                            log.info("business successfully updated");
                                            return Mono.just(Response.<Business>builder()
                                                            .status(HttpStatus.OK)
                                                            .statusCode(HttpStatus.OK.value())
                                                            .data(business1)
                                                            .message(SUCCESSFUL)
                                                    .build());

                                        })
                                        .doOnError(throwable -> {
                                            log.error("error updating business for merchant {}",user.getMerchantId());
                                            throw new CustomException(Response.<Business>builder()
                                                    .message(FAILED)
                                                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                    .errors(List.of("error updating business",throwable.getLocalizedMessage(),throwable.getMessage()))
                                                    .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                                        });

                            })
                            .switchIfEmpty(Mono.defer(() -> {
                                log.error("business does not exist for merchant {}",user.getFirstName());
                                return Mono.just(Response.<Business>builder()
                                        .status(HttpStatus.NOT_FOUND)
                                        .statusCode(HttpStatus.NOT_FOUND.value())
                                        .message(FAILED)
                                        .errors(List.of("Business not found to update"))
                                        .build());
                            }));
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("user not logged in or exist");
                    return Mono.just(Response.<Business>builder()
                            .status(HttpStatus.NOT_FOUND)
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .message(FAILED)
                            .errors(List.of("user not logged in or found"))
                            .build());
                })).doOnError(throwable -> {
                    log.error("error fetching logged in user, error is {}", throwable.getLocalizedMessage());
                    throw new CustomException(Response.<Void>builder()
                            .message(FAILED)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .errors(List.of("error fetching logged in user",throwable.getLocalizedMessage(),throwable.getMessage()))
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @Override
    public Mono<Response<Business>> delete(BusinessDTO request) {
        return null;
    }

    @Override
    public Mono<Response<Business>> view() {
        log.info("about viewing merchant business");
        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    return businessRepository.findBusinessByMerchantId(user.getMerchantId())
                            .flatMap(business -> {
                                log.info("business found");
                                return Mono.just(Response.<Business>builder()
                                        .status(HttpStatus.OK)
                                        .statusCode(HttpStatus.OK.value())
                                        .data(business)
                                        .message(SUCCESSFUL)
                                        .build());

                            })
                            .switchIfEmpty(Mono.defer(() -> {
                                log.error("business not found for merchant {}",user.getFirstName());
                                return Mono.just(Response.<Business>builder()
                                        .status(HttpStatus.NOT_FOUND)
                                        .statusCode(HttpStatus.NOT_FOUND.value())
                                        .message(FAILED)
                                        .errors(List.of("Business not found"))
                                        .build());
                            }));
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("user not logged in or exist");
                    return Mono.just(Response.<Business>builder()
                            .status(HttpStatus.NOT_FOUND)
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .message(FAILED)
                            .errors(List.of("user not logged in or found"))
                            .build());
                })).doOnError(throwable -> {
                    log.error("error fetching logged in user, error is {}", throwable.getLocalizedMessage());
                    throw new CustomException(Response.<Void>builder()
                            .message(FAILED)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .errors(List.of("error fetching logged in user",throwable.getLocalizedMessage(),throwable.getMessage()))
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

}
