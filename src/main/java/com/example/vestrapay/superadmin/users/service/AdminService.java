package com.example.vestrapay.superadmin.users.service;

import com.example.vestrapay.exceptions.CustomException;
import com.example.vestrapay.merchant.authentications.interfaces.IAuthenticationService;
import com.example.vestrapay.merchant.business.models.Business;
import com.example.vestrapay.merchant.business.repository.BusinessRepository;
import com.example.vestrapay.merchant.roles_and_permissions.models.RolePermission;
import com.example.vestrapay.merchant.roles_and_permissions.repository.RolePermissionRepository;
import com.example.vestrapay.merchant.transactions.models.Transaction;
import com.example.vestrapay.merchant.transactions.reporitory.TransactionRepository;
import com.example.vestrapay.merchant.users.enums.UserType;
import com.example.vestrapay.merchant.users.models.User;
import com.example.vestrapay.merchant.users.repository.UserRepository;
import com.example.vestrapay.superadmin.users.dto.EnableDisableDTO;
import com.example.vestrapay.superadmin.users.dto.MerchantDTO;
import com.example.vestrapay.utils.dtos.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService implements IAdminService {
    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final TransactionRepository transactionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final IAuthenticationService authenticationService;
    private static final String SUCCESSFUL  = "SUCCESSFUl";
    private static final String FAILED  = "FAILED";
    @Override
    public Mono<Response<List<User>>> viewAllAdmin() {
        return userRepository.findByUserType(UserType.ADMIN)
                .collectList()
                .flatMap(users -> Mono.just(Response.<List<User>>builder()
                                .data(users)
                                .status(HttpStatus.OK)
                                .statusCode(HttpStatus.OK.value())
                                .message(SUCCESSFUL)
                        .build()))
                .switchIfEmpty(Mono.defer(() -> Mono.just(Response.<List<User>>builder()
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .status(HttpStatus.NOT_FOUND)
                                .message(FAILED)
                                .errors(List.of("Admins not found"))
                        .build())));
    }

    @Override
    public Mono<Response<List<User>>> viewAllMerchants() {
        return userRepository.findByUserType(UserType.MERCHANT)
                .collectList()
                .flatMap(users -> Mono.just(Response.<List<User>>builder()
                        .data(users)
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .message(SUCCESSFUL)
                        .build()))
                .switchIfEmpty(Mono.defer(() -> Mono.just(Response.<List<User>>builder()
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .status(HttpStatus.NOT_FOUND)
                        .message(FAILED)
                        .errors(List.of("Admins not found"))
                        .build())));    }

    @Override
    public Mono<Response<MerchantDTO>> viewMerchantById(String merchantId) {
        return userRepository.findByMerchantIdAndUuid(merchantId,merchantId)
                .flatMap(user -> {
                    Flux<User> merchantUsers = userRepository.findByMerchantId(merchantId);
                    Flux<Transaction> transactions = transactionRepository.findByMerchantId(merchantId);
                    Mono<Business> merchantBusiness = businessRepository.findBusinessByMerchantId(merchantId);
                    Flux<RolePermission> merchantRoles = rolePermissionRepository.findByUserId(merchantId);


                    return merchantUsers.collectList().flatMap(users -> {
                        return transactions.collectList().flatMap(transactions1 -> {
                            return merchantBusiness.flatMap(business -> {
                                return merchantRoles.collectList().flatMap(rolePermissions -> {
                                    MerchantDTO build = MerchantDTO.builder()
                                            .merchantPermissions(rolePermissions)
                                            .merchantBusiness(business)
                                            .merchantTransaction(transactions1)
                                            .merchantUsers(users)
                                            .merchant(user)
                                            .build();
                                    log.info("admin get user response is {}",build);
                                    return Mono.just(Response.<MerchantDTO>builder()
                                                    .message(SUCCESSFUL)
                                                    .statusCode(HttpStatus.OK.value())
                                                    .status(HttpStatus.OK)
                                                    .data(build)
                                            .build());
                                });
                            });
                        });
                    });


                });
    }

    @Override
    public Mono<Response<Boolean>> disableMerchant(String merchantId) {
        return userRepository.findByMerchantId(merchantId)
                .collectList()
                .flatMap(users -> {
                    users.forEach(user -> user.setEnabled(false));

                    return userRepository.saveAll(users)
                            .collectList()
                            .flatMap(userList -> Mono.just(Response.<Boolean>builder()
                                            .data(Boolean.TRUE)
                                    .message(SUCCESSFUL)
                                    .statusCode(HttpStatus.OK.value())
                                    .status(HttpStatus.OK)
                                    .build()));
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("merchant not found with id {}",merchantId);
                    return Mono.just(Response.<Boolean>builder()
                                    .errors(List.of("merchant not found"))
                                    .message(FAILED)
                                    .statusCode(HttpStatus.NOT_FOUND.value())
                                    .status(HttpStatus.NOT_FOUND)
                            .build());

                }))
                .doOnError(throwable -> {
                    log.error("error disabling merchant. error is {}",throwable.getMessage());
                    throw new CustomException(Response.builder()
                            .message(FAILED)
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .data(throwable.getLocalizedMessage())
                            .errors(List.of(throwable.getMessage()))
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @Override
    public Mono<Response<Boolean>> enableMerchant(String merchantId) {
        return userRepository.findByMerchantId(merchantId)
                .collectList()
                .flatMap(users -> {
                    users.forEach(user -> user.setEnabled(true));

                    return userRepository.saveAll(users)
                            .collectList()
                            .flatMap(userList -> {
                                return Mono.just(Response.<Boolean>builder()
                                        .data(Boolean.TRUE)
                                        .message(SUCCESSFUL)
                                        .statusCode(HttpStatus.OK.value())
                                        .status(HttpStatus.OK)
                                        .build());
                            });
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("merchant not found with id {}",merchantId);
                    return Mono.just(Response.<Boolean>builder()
                            .errors(List.of("merchant not found"))
                            .message(FAILED)
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .status(HttpStatus.NOT_FOUND)
                            .build());

                }))
                .doOnError(throwable -> {
                    log.error("error disabling merchant. error is {}",throwable.getMessage());
                    throw new CustomException(Response.builder()
                            .message(FAILED)
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .data(throwable.getLocalizedMessage())
                            .errors(List.of(throwable.getMessage()))
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });    }

    @Override
    public Mono<Response<Object>> enableDisableAdmin(EnableDisableDTO request) {
        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    if (!user.getUserType().equals(UserType.SUPER_ADMIN)){
                        log.error("only superadmin can delete a user");
                        return Mono.just(Response.builder()
                                        .errors(List.of("logged in user not a super admin"))
                                        .status(HttpStatus.UNAUTHORIZED)
                                        .statusCode(HttpStatus.UNAUTHORIZED.value())
                                        .message(FAILED)
                                .build());
                    }
                    return userRepository.findByUserTypeAndUuid(UserType.ADMIN,request.getAdminUUID())
                            .flatMap(user1 -> {
                                user1.setEnabled(request.getValue());
                                return userRepository.save(user1)
                                        .flatMap(user2 -> {
                                            return Mono.just(Response.builder()
                                                    .errors(List.of("user to disable not found or not an admin"))
                                                    .status(HttpStatus.OK)
                                                    .statusCode(HttpStatus.OK.value())
                                                    .message(SUCCESSFUL)
                                                            .data(user2)
                                                    .build());
                                        });

                            }).switchIfEmpty(Mono.defer(() -> {
                                log.error("user to disable not found or not an admin");
                                return Mono.just(Response.builder()
                                        .errors(List.of("user to disable not found or not an admin"))
                                        .status(HttpStatus.NOT_FOUND)
                                        .statusCode(HttpStatus.NOT_FOUND.value())
                                        .message(FAILED)
                                        .build());
                            }));
                }).switchIfEmpty(Mono.defer(() -> {
                    return Mono.just(Response.builder()
                            .errors(List.of("user not logged in"))
                            .status(HttpStatus.UNAUTHORIZED)
                            .statusCode(HttpStatus.UNAUTHORIZED.value())
                            .message(FAILED)
                            .build());
                }));
    }
}
