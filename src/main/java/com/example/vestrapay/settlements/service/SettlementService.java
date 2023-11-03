package com.example.vestrapay.settlements.service;

import com.example.vestrapay.audits.models.AuditEvent;
import com.example.vestrapay.audits.services.AuditLogService;
import com.example.vestrapay.authentications.interfaces.IAuthenticationService;
import com.example.vestrapay.exceptions.CustomException;
import com.example.vestrapay.settlements.dtos.SettlementDTO;
import com.example.vestrapay.settlements.enums.SettlementEnum;
import com.example.vestrapay.settlements.interfaces.ISettlementService;
import com.example.vestrapay.settlements.models.Settlement;
import com.example.vestrapay.settlements.models.SettlementDurations;
import com.example.vestrapay.settlements.models.WemaAccounts;
import com.example.vestrapay.settlements.repository.SettlementDurationRepository;
import com.example.vestrapay.settlements.repository.SettlementRepository;
import com.example.vestrapay.settlements.repository.WemaAccountRepository;
import com.example.vestrapay.users.enums.UserType;
import com.example.vestrapay.users.models.User;
import com.example.vestrapay.utils.dtos.Response;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.example.vestrapay.utils.dtos.Constants.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class SettlementService implements ISettlementService {
    private final SettlementRepository settlementRepository;
    private final IAuthenticationService authenticationService;
    private final ModelMapper modelMapper;
    private final AuditLogService auditLogService;
    private final WemaAccountRepository wemaAccountRepository;
    private final SettlementDurationRepository settlementDurationRepository;
    @Value("${account.creation.prefix}")
    String wemaPrefix;


    @Override
    public Mono<Response<Settlement>> addAccount(SettlementDTO request) {
        log.info("about adding account with DTO {}",request.toString());
        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    log.info("user for account gotten. {}",user.toString());
                    Settlement settlement = modelMapper.map(request, Settlement.class);
                    settlement.setUuid(UUID.randomUUID().toString());
                    settlement.setMerchantId(user.getMerchantId());
                    return settlementRepository.findByAccountNumberAndMerchantId(request.getAccountNumber(), user.getUuid())
                            .flatMap(settlement1 -> {
                                log.error("account already exist for user {}",user.getEmail());
                                return Mono.just(Response.<Settlement>builder()
                                        .statusCode(HttpStatus.CONFLICT.value())
                                        .status(HttpStatus.CONFLICT)
                                        .message(FAILED)
                                        .errors(List.of("account already exist for user"))
                                        .build());
                            })
                            .switchIfEmpty(Mono.defer(() -> settlementRepository.save(settlement)
                                    .flatMap(settlement1 -> {
                                        log.info("settlement successfully created. {}",settlement1);
                                        return Mono.just(Response.<Settlement>builder()
                                                        .data(settlement1)
                                                        .message(SUCCESSFUL)
                                                        .status(HttpStatus.CREATED)
                                                        .statusCode(HttpStatus.CREATED.value())
                                                .build());
                                    })
                                    .doOnError(throwable -> {
                                        log.error("error saving settlement account for user, error is {}",throwable.getLocalizedMessage());
                                        throw new CustomException(Response.<Settlement>builder()
                                                .message(FAILED)
                                                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .errors(List.of(throwable.getLocalizedMessage(),throwable.getMessage()))
                                                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                                    })))
                            .doOnError(throwable -> {
                                log.error("error adding settlement account, error is {}",throwable.getLocalizedMessage());
                                throw new CustomException(Response.<Settlement>builder()
                                        .message(FAILED)
                                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .errors(List.of(throwable.getLocalizedMessage(),throwable.getMessage()))
                                        .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                            });

                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error(USER_NOT_LOGGED_IN);
                    return Mono.just(Response.<Settlement>builder()
                            .status(HttpStatus.UNAUTHORIZED)
                            .statusCode(HttpStatus.UNAUTHORIZED.value())
                            .message(FAILED)
                            .errors(List.of(USER_NOT_LOGGED_IN))
                            .build());
                }))
                .doOnError(throwable -> {
                    log.error(ERROR_FETCHING_USER);
                    throw new CustomException(Response.<Settlement>builder()
                            .message(FAILED)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .errors(List.of(ERROR_FETCHING_USER,throwable.getLocalizedMessage(),throwable.getMessage()))
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });

    }

    @Override
    public Mono<Response<WemaAccounts>> generateWemaAccountForMerchant(@NotNull User merchant) {
        if (!merchant.getUserType().equals(UserType.MERCHANT)){
            return Mono.just(Response.<WemaAccounts>builder()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .status(HttpStatus.UNAUTHORIZED)
                    .message(FAILED)
                    .errors(List.of("Only Merchants can create account"))
                    .build());
        }
        log.info("user gotten about generating wema account for merchant");
        return wemaAccountRepository.findByMerchantId(merchant.getMerchantId())
                .flatMap(wemaAccounts -> {
                    log.error("wema account already generated for merchant");
                    return Mono.just(Response.<WemaAccounts>builder()
                            .data(wemaAccounts)
                            .message(SUCCESSFUL)
                            .status(HttpStatus.OK)
                            .statusCode(HttpStatus.OK.value())
                            .build());
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("about creating wema account for merchant {}",merchant.getMerchantId());
                    //since Id is always unique on the DB as primary key
                    String wemaAccountNumber = generateAccountNumber(merchant.getId());
                    WemaAccounts wemaAccount = WemaAccounts.builder()
                            .merchantId(merchant.getMerchantId())
                            .accountName(merchant.getBusinessName())
                            .accountNumber(wemaAccountNumber)
                            .accountName(merchant.getFirstName().concat(" ").concat(merchant.getLastName()))
                            .uuid(UUID.randomUUID().toString())
                            .build();

                    return wemaAccountRepository.save(wemaAccount)
                            .flatMap(wemaAccounts -> {
                                log.info("account successfully created for merchant");
                                return Mono.just(Response.<WemaAccounts>builder()
                                        .data(wemaAccounts)
                                        .message(SUCCESSFUL)
                                        .status(HttpStatus.CREATED)
                                        .statusCode(HttpStatus.CREATED.value())
                                        .build());
                            })
                            .doOnError(throwable -> {
                                log.error("Error generating wema account. error is {}",throwable.getLocalizedMessage());
                                throw new CustomException(Response.builder()
                                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .message(FAILED)
                                        .errors(List.of("Error creating wema account. error is "+throwable.getLocalizedMessage()))
                                        .build(), HttpStatus.INTERNAL_SERVER_ERROR);

                            });
                })).doOnError(throwable -> {
                    log.error("Error generating wema account. error is {}",throwable.getLocalizedMessage());
                    throw new CustomException(Response.builder()
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message(FAILED)
                            .errors(List.of("Error creating wema account. error is "+throwable.getLocalizedMessage()))
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);

                });
    }

    @Override
    public Mono<Response<WemaAccounts>> viewWemaAccount() {
        log.info("about viewing wema account for merchant");
        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    log.info("user gotten {}",user.getUuid());
                    return wemaAccountRepository.findByMerchantId(user.getMerchantId())
                            .flatMap(wemaAccounts -> {
                                log.info("Wema account gotten for merchant {}",user.getMerchantId());
                                return Mono.just(Response.<WemaAccounts>builder()
                                                .data(wemaAccounts)
                                                .statusCode(HttpStatus.OK.value())
                                                .status(HttpStatus.OK)
                                                .message(SUCCESSFUL)
                                        .build());
                            })
                            .switchIfEmpty(Mono.defer(() -> {
                                log.error("account not found for merchant. create account");
                                return Mono.just(Response.<WemaAccounts>builder()
                                                .message(FAILED)
                                                .status(HttpStatus.NOT_FOUND)
                                                .statusCode(HttpStatus.NOT_FOUND.value())
                                                .errors(List.of("Wema account not found for user"))
                                        .build());
                            }));
                });
    }

    @Override
    public Mono<Response<Settlement>> updateAccount(Settlement request) {
        log.info("about updating account with DTO {}",request.toString());
        return authenticationService.getLoggedInUser()
                .flatMap(user -> settlementRepository.findByUuidAndMerchantId(request.getUuid(), user.getUuid())
                        .flatMap(settlement -> {
                            modelMapper.map(request,settlement);
                            return settlementRepository.save(settlement)
                                    .flatMap(settlement1 -> {
                                        log.info("settlement account successfully updated");
                                        auditLogService.log(AuditEvent.builder()
                                                        .uuid(UUID.randomUUID().toString())
                                                        .userUUID(user.getUuid())
                                                        .event("updated settlement account from ".concat(settlement.toString()).concat("to ").concat(settlement1.toString()))
                                                .build());
                                        return Mono.just(Response.<Settlement>builder()
                                                        .data(settlement1)
                                                        .message(SUCCESSFUL)
                                                        .status(HttpStatus.OK)
                                                        .statusCode(HttpStatus.OK.value())
                                                .build());
                                    }).doOnError(throwable -> {
                                        log.error("error saving settlement for user {}",user.getEmail());
                                        throw new CustomException(Response.<Settlement>builder()
                                                .message(FAILED)
                                                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .errors(List.of("error saving settlement account for user",throwable.getLocalizedMessage(),throwable.getMessage()))
                                                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                                    });
                        })
                        .switchIfEmpty(Mono.defer(() -> {
                            log.info("settlement account not found for user {}",user.getEmail());
                            return Mono.just(Response.<Settlement>builder()
                                            .statusCode(HttpStatus.NOT_FOUND.value())
                                            .status(HttpStatus.NOT_FOUND)
                                            .message(FAILED)
                                            .errors(List.of("settlement account not found"))
                                    .build());
                        }))
                        .doOnError(throwable -> {
                            log.error("error fetching settlement account for user {}",user.getEmail());
                            throw new CustomException(Response.<Settlement>builder()
                                    .message(FAILED)
                                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .errors(List.of("error fetching settlement account for user",throwable.getLocalizedMessage(),throwable.getMessage()))
                                    .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                        }))
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("user not logged in");
                    return Mono.just(Response.<Settlement>builder()
                            .status(HttpStatus.UNAUTHORIZED)
                            .statusCode(HttpStatus.UNAUTHORIZED.value())
                            .message(FAILED)
                            .errors(List.of("user not logged in"))
                            .build());
                }))
                .doOnError(throwable -> {
                    log.error(ERROR_FETCHING_USER);
                    throw new CustomException(Response.<Settlement>builder()
                            .message(FAILED)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .errors(List.of(ERROR_FETCHING_USER,throwable.getLocalizedMessage(),throwable.getMessage()))
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @Override
    public Mono<Response<Void>> removeAccount(Settlement request) {
        log.info("about removing account with DTO {}",request.toString());
        return authenticationService.getLoggedInUser()
                .flatMap(user -> settlementRepository.findByUuidAndMerchantId(request.getUuid(),request.getMerchantId())
                        .flatMap(settlement -> {
                            settlementRepository.delete(settlement).subscribe();
                            return Mono.just(Response.<Void>builder()
                                    .message(SUCCESSFUL)
                                    .status(HttpStatus.OK)
                                    .statusCode(HttpStatus.OK.value())
                                    .build());
                        })
                        .switchIfEmpty(Mono.defer(() -> {
                            log.info("settlement account not found for user {}",user.getEmail());
                            return Mono.just(Response.<Void>builder()
                                    .statusCode(HttpStatus.NOT_FOUND.value())
                                    .status(HttpStatus.NOT_FOUND)
                                    .message(FAILED)
                                    .errors(List.of("settlement account not found"))
                                    .build());
                        }))
                        .doOnError(throwable -> {
                            log.error("error fetching settlement account for user {}",user.getEmail());
                            throw new CustomException(Response.<Settlement>builder()
                                    .message(FAILED)
                                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .errors(List.of("error fetching settlement account for user",throwable.getLocalizedMessage(),throwable.getMessage()))
                                    .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                        }))
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("user not logged in");
                    return Mono.just(Response.<Void>builder()
                            .status(HttpStatus.UNAUTHORIZED)
                            .statusCode(HttpStatus.UNAUTHORIZED.value())
                            .message(FAILED)
                            .errors(List.of("user not logged in"))
                            .build());
                }))
                .doOnError(throwable -> {
                    log.error(ERROR_FETCHING_USER);
                    throw new CustomException(Response.<Settlement>builder()
                            .message(FAILED)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .errors(List.of(ERROR_FETCHING_USER,throwable.getLocalizedMessage(),throwable.getMessage()))
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @Override
    public Mono<Response<Void>> setPrimaryAccount(String settlementUUID) {
        String UUID = settlementUUID.replace("\"","");
        log.info("about setting account {} as primary account",settlementUUID);
        return authenticationService.getLoggedInUser()
                .flatMap(user -> settlementRepository.findByUuidAndMerchantId(UUID, user.getMerchantId())
                        .flatMap(settlement -> settlementRepository.resetPrimarySettlementToFalseForUser(user.getMerchantId())
                                .flatMap(o -> {
                                    settlement.setPrimaryAccount(true);
                                    return settlementRepository.save(settlement)
                                            .flatMap(settlement1 -> {
                                                log.info("primary account updated");
                                                return Mono.just(Response.<Void>builder()
                                                                .statusCode(HttpStatus.OK.value())
                                                                .status(HttpStatus.OK)
                                                                .message(SUCCESSFUL)
                                                        .build());
                                            }).doOnError(throwable -> {
                                                log.error("error saving primary account, error is {}",throwable.getLocalizedMessage());
                                                throw new CustomException(Response.<Void>builder()
                                                        .message(FAILED)
                                                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                        .errors(List.of("error saving primary account",ERROR_FETCHING_USER,throwable.getLocalizedMessage(),throwable.getMessage()))
                                                        .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                                            });
                                }).doOnError(throwable -> {
                                    log.error("error resetting previous primary account, error is {}",throwable.getLocalizedMessage());
                                    throw new CustomException(Response.<Void>builder()
                                            .message(FAILED)
                                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                            .errors(List.of("error resetting previous primary account",ERROR_FETCHING_USER,throwable.getLocalizedMessage(),throwable.getMessage()))
                                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                                }))
                        .switchIfEmpty(Mono.defer(() -> {
                            log.info("settlement account not found for user {}",user.getEmail());
                            return Mono.just(Response.<Void>builder()
                                    .statusCode(HttpStatus.NOT_FOUND.value())
                                    .status(HttpStatus.NOT_FOUND)
                                    .message(FAILED)
                                    .errors(List.of("settlement account not found"))
                                    .build());
                        }))
                        .doOnError(throwable -> {
                            log.error("error fetching settlement account for user {}",user.getEmail());
                            throw new CustomException(Response.<Settlement>builder()
                                    .message(FAILED)
                                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .errors(List.of("error fetching settlement account for user",throwable.getLocalizedMessage(),throwable.getMessage()))
                                    .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                        }))
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("user not logged in");
                    return Mono.just(Response.<Void>builder()
                            .status(HttpStatus.UNAUTHORIZED)
                            .statusCode(HttpStatus.UNAUTHORIZED.value())
                            .message(FAILED)
                            .errors(List.of("user not logged in"))
                            .build());
                }))
                .doOnError(throwable -> {
                    log.error(ERROR_FETCHING_USER);
                    throw new CustomException(Response.<Settlement>builder()
                            .message(FAILED)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .errors(List.of(ERROR_FETCHING_USER,throwable.getLocalizedMessage(),throwable.getMessage()))
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @Override
    public Mono<Response<List<Settlement>>> viewAllUserAccounts() {
        return authenticationService.getLoggedInUser().flatMap(user -> {
            return settlementRepository.findByMerchantId(user.getUuid())
                    .collectList().flatMap(settlements -> {
                        log.info("settlement accounts gotten");
                        return Mono.just(Response.<List<Settlement>>builder()
                                        .message(SUCCESSFUL)
                                        .data(settlements)
                                        .statusCode(HttpStatus.OK.value())
                                        .status(HttpStatus.OK)
                                .build());
                    }).doOnError(throwable -> {
                        log.error("error fetching accounts. error is {}",throwable.getLocalizedMessage());
                        throw new CustomException(Response.<List<Settlement>>builder()
                                .message(FAILED)
                                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .errors(List.of("error fetching accounts for user".concat(user.getEmail()),throwable.getLocalizedMessage(),throwable.getMessage()))
                                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                    });

        }).switchIfEmpty(Mono.defer(() -> {
            log.error("user not logged in or found");
            return Mono.just(Response.<List<Settlement>>builder()
                    .status(HttpStatus.UNAUTHORIZED)
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .message(FAILED)
                    .errors(List.of("user not logged in"))
                    .build());

        })).doOnError(throwable -> {
            log.error("error getting logged in user. error is {}",throwable.getLocalizedMessage());
            throw new CustomException(Response.<List<Settlement>>builder()
                    .message(FAILED)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .errors(List.of(ERROR_FETCHING_USER,throwable.getLocalizedMessage(),throwable.getMessage()))
                    .build(), HttpStatus.INTERNAL_SERVER_ERROR);
        });
    }

    @Override
    public Mono<Response<Settlement>> viewAccount(String uuid) {
        return authenticationService.getLoggedInUser().flatMap(user -> {
            return settlementRepository.findByUuidAndMerchantId(uuid,user.getUuid())
                    .flatMap(settlement -> {
                        log.info("settlement accounts gotten {}",settlement.toString());
                        return Mono.just(Response.<Settlement>builder()
                                .message(SUCCESSFUL)
                                .data(settlement)
                                .statusCode(HttpStatus.OK.value())
                                .status(HttpStatus.OK)
                                .build());
                    }).doOnError(throwable -> {
                        log.error("error fetching account. error is {}",throwable.getLocalizedMessage());
                        throw new CustomException(Response.<Settlement>builder()
                                .message(FAILED)
                                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .errors(List.of("error fetching account for user".concat(user.getEmail()),throwable.getLocalizedMessage(),throwable.getMessage()))
                                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                    });

        }).switchIfEmpty(Mono.defer(() -> {
            log.error("user not logged in or found");
            return Mono.just(Response.<Settlement>builder()
                    .status(HttpStatus.UNAUTHORIZED)
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .message(FAILED)
                    .errors(List.of("user not logged in"))
                    .build());

        })).doOnError(throwable -> {
            log.error("error getting logged in user. error is {}",throwable.getLocalizedMessage());
            throw new CustomException(Response.<Settlement>builder()
                    .message(FAILED)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .errors(List.of(ERROR_FETCHING_USER,throwable.getLocalizedMessage(),throwable.getMessage()))
                    .build(), HttpStatus.INTERNAL_SERVER_ERROR);
        });
    }

    @Override
    public Mono<Response<Settlement>> viewPrimaryAccount() {
        return authenticationService.getLoggedInUser().flatMap(user -> {
            return settlementRepository.findByMerchantIdAndPrimaryAccount(user.getUuid(),true)
                    .flatMap(settlement -> {
                        log.info("primary settlement account gotten {}",settlement.toString());
                        return Mono.just(Response.<Settlement>builder()
                                .message(SUCCESSFUL)
                                .data(settlement)
                                .statusCode(HttpStatus.OK.value())
                                .status(HttpStatus.OK)
                                .build());
                    }).doOnError(throwable -> {
                        log.error("error fetching primary account. error is {}",throwable.getLocalizedMessage());
                        throw new CustomException(Response.<Settlement>builder()
                                .message(FAILED)
                                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .errors(List.of("error fetching primary account for user".concat(user.getEmail()),throwable.getLocalizedMessage(),throwable.getMessage()))
                                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                    });

        }).switchIfEmpty(Mono.defer(() -> {
            log.error("user not logged in or found");
            return Mono.just(Response.<Settlement>builder()
                    .status(HttpStatus.UNAUTHORIZED)
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .message(FAILED)
                    .errors(List.of("user not logged in"))
                    .build());

        })).doOnError(throwable -> {
            log.error("error getting logged in user. error is {}",throwable.getLocalizedMessage());
            throw new CustomException(Response.<Settlement>builder()
                    .message(FAILED)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .errors(List.of(ERROR_FETCHING_USER,throwable.getLocalizedMessage(),throwable.getMessage()))
                    .build(), HttpStatus.INTERNAL_SERVER_ERROR);
        });
    }

    @Override
    public Mono<Response<List<SettlementEnum>>> settlementDurations() {

        return Mono.just(Response.<List<SettlementEnum>>builder()
                .data(new ArrayList<>(List.of(SettlementEnum.values())))
                .message("Successful")
                .statusCode(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .build()).cache(Duration.ofMinutes(3600));
    }


    private String generateAccountNumber(Long id){
        try {
            return wemaPrefix+padleft(String.valueOf(id),7,'0');

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String padleft(String s, int len, char c) throws Exception {
        s = s.trim();
        if (s.length() > len) {
            throw new Exception("invalid len " + s.length() + "/" + len);
        } else {
            StringBuilder d = new StringBuilder(len);
            int fill = len - s.length();

            while(fill-- > 0) {
                d.append(c);
            }

            d.append(s);
            return d.toString();
        }
    }
}
