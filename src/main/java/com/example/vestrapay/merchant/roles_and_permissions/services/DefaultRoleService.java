package com.example.vestrapay.merchant.roles_and_permissions.services;

import com.example.vestrapay.exceptions.CustomException;
import com.example.vestrapay.merchant.roles_and_permissions.interfaces.IPermissionService;
import com.example.vestrapay.merchant.roles_and_permissions.models.RolePermission;
import com.example.vestrapay.merchant.roles_and_permissions.repository.RolePermissionRepository;
import com.example.vestrapay.utils.dtos.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.UUID;

import static com.example.vestrapay.utils.dtos.Constants.FAILED;

@RequiredArgsConstructor
@Service
@Slf4j
public class DefaultRoleService {
    private final RolePermissionRepository rolePermissionRepository;
    private final IPermissionService permissionService;


    public Mono<Void> createDefaultRole(String userId, String merchantId, String userType) {
        Mono<Boolean> processMono = Mono.fromCallable(() -> {
            if (userType.equalsIgnoreCase("MERCHANT")){
                createMerchantRole(userId,merchantId).subscribe();
            }
            else if (userType.equalsIgnoreCase("MERCHANT_USER")){
                createMerchantUser(userId,merchantId).subscribe();
            }
            else {
                createAdminRole(userId).subscribe();

            }
            return true;
        });
        processMono.subscribeOn(Schedulers.boundedElastic()).subscribe();
        return Mono.empty();
    }
    private Mono<Response<Boolean>> createMerchantRole(String userId, String merchantId){
        log.info("creating default merchant role for merchant {}",userId);
        return Mono.defer(() -> {
            return rolePermissionRepository.findByUserId(userId).collectList().flatMap(rolePermission -> {
                if (rolePermission.isEmpty()){
                    return permissionService.getAllMerchantPermissions().flatMap(listResponse -> {
                        listResponse.getData().forEach(permissions -> {
                            log.info("creating permission {} for user {}",permissions,userId);
                            rolePermissionRepository.save(RolePermission.builder()
                                    .uuid(UUID.randomUUID().toString())
                                    .userId(userId)
                                    .merchantID(merchantId)
                                    .permissionId(permissions.getPermissionName())
                                    .build()).subscribe();

                        });

                        log.info("merchant roles created");
                        return Mono.just(Response.<Boolean>builder()
                                .data(Boolean.TRUE)
                                .build());
                    });

                }
                else {
                    log.error("role permission already exists for user {}",userId);
                    return Mono.just(Response.<Boolean>builder()
                            .message(FAILED)
                            .status(HttpStatus.CONFLICT)
                            .build());
                }

            }).switchIfEmpty(Mono.defer(() -> {
                return permissionService.getAllMerchantPermissions().flatMap(listResponse -> {
                    listResponse.getData().forEach(permissions -> {
                        log.info("creating merchant role for user");
                        rolePermissionRepository.save(RolePermission.builder()
                                .uuid(UUID.randomUUID().toString())
                                .userId(userId)
                                .permissionId(permissions.getPermissionName())
                                .build()).subscribe();

                    });

                    log.info("merchant roles created");
                    return Mono.just(Response.<Boolean>builder()
                            .data(Boolean.TRUE)
                            .build());
                });
            })).doOnError(throwable -> {
                log.error("error fetching logged in user. error is {}",throwable.getLocalizedMessage());
                throw new CustomException(Response.builder()
                        .message(FAILED)
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .errors(List.of("error fetching logged in user",throwable.getLocalizedMessage()))
                        .build(), HttpStatus.INTERNAL_SERVER_ERROR);
            });

        }).doOnError(throwable -> {
            log.error("error creating default merchant role. error is {}",throwable.getLocalizedMessage());
            throw new CustomException();
        });
    }
    private Mono<Response<Boolean>> createMerchantUser(String userId,String merchantId){
        log.info("creating default merchant role for merchant {}",userId);
        return Mono.defer(() -> {
            return rolePermissionRepository.findByUserId(userId).collectList().flatMap(rolePermission -> {
                if (rolePermission.isEmpty()){
                    return permissionService.getAllMerchantUserPermissions().flatMap(listResponse -> {
                        listResponse.getData().forEach(permissions -> {
                            log.info("creating permission {} for user {}",permissions,userId);
                            rolePermissionRepository.save(RolePermission.builder()
                                    .uuid(UUID.randomUUID().toString())
                                    .userId(userId)
                                    .permissionId(permissions.getPermissionName())
                                    .build()).subscribe();

                        });

                        log.info("merchant roles created");
                        return Mono.just(Response.<Boolean>builder()
                                .data(Boolean.TRUE)
                                .build());
                    });

                }
                else {
                    log.error("role permission already exists for user {}",userId);
                    return Mono.just(Response.<Boolean>builder()
                            .message(FAILED)
                            .status(HttpStatus.CONFLICT)
                            .build());
                }

            }).switchIfEmpty(Mono.defer(() -> {
                return permissionService.getAllMerchantPermissions().flatMap(listResponse -> {
                    listResponse.getData().forEach(permissions -> {
                        log.info("creating merchant role for user");
                        rolePermissionRepository.save(RolePermission.builder()
                                .uuid(UUID.randomUUID().toString())
                                .merchantID(merchantId)
                                .userId(userId)
                                .permissionId(permissions.getPermissionName())
                                .build()).subscribe();

                    });

                    log.info("merchant roles created");
                    return Mono.just(Response.<Boolean>builder()
                            .data(Boolean.TRUE)
                            .build());
                });
            })).doOnError(throwable -> {
                log.error("error fetching logged in user. error is {}",throwable.getLocalizedMessage());
                throw new CustomException(Response.builder()
                        .message(FAILED)
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .errors(List.of("error fetching logged in user",throwable.getLocalizedMessage()))
                        .build(), HttpStatus.INTERNAL_SERVER_ERROR);
            });

        }).doOnError(throwable -> {
            log.error("error creating default merchant role. error is {}",throwable.getLocalizedMessage());
            throw new CustomException();
        });
    }
    private Mono<Response<Boolean>> createAdminRole(String userId){
        log.info("creating default admin role for user {}",userId);
        return Mono.defer(() -> {
            return rolePermissionRepository.findByUserId(userId).collectList().flatMap(rolePermission -> {
                if (rolePermission.isEmpty()){
                    return permissionService.getAllPermissions().flatMap(listResponse -> {
                        listResponse.getData().forEach(permissions -> {
                            if (!permissions.getPermissionName().equals("DELETE_ADMIN")){
                                log.info("creating permission {} for user {}",permissions,userId);
                                rolePermissionRepository.save(RolePermission.builder()
                                        .uuid(UUID.randomUUID().toString())
                                        .userId(userId)
                                        .permissionId(permissions.getPermissionName())
                                        .build()).subscribe();
                            }

                        });

                        log.info("admin roles created");
                        return Mono.just(Response.<Boolean>builder()
                                .data(Boolean.TRUE)
                                .build());
                    });

                }
                else {
                    log.error("role permission already exists for admin {}",userId);
                    return Mono.just(Response.<Boolean>builder()
                            .message(FAILED)
                            .status(HttpStatus.CONFLICT)
                            .build());
                }

            }).switchIfEmpty(Mono.defer(() -> {
                return permissionService.getAllPermissions().flatMap(listResponse -> {
                    listResponse.getData().forEach(permissions -> {
                        log.info("creating merchant role for user");
                        rolePermissionRepository.save(RolePermission.builder()
                                .uuid(UUID.randomUUID().toString())
                                .merchantID("VESTRAPAY")
                                .userId(userId)
                                .permissionId(permissions.getPermissionName())
                                .build()).subscribe();

                    });

                    log.info("merchant roles created");
                    return Mono.just(Response.<Boolean>builder()
                            .data(Boolean.TRUE)
                            .build());
                });
            })).doOnError(throwable -> {
                log.error("error fetching logged in user. error is {}",throwable.getLocalizedMessage());
                throw new CustomException(Response.builder()
                        .message(FAILED)
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .errors(List.of("error fetching logged in user",throwable.getLocalizedMessage()))
                        .build(), HttpStatus.INTERNAL_SERVER_ERROR);
            });

        }).doOnError(CustomException.class, throwable -> {
            log.error("error creating default merchant role. error is {}",throwable.getLocalizedMessage());
            throw new CustomException(Response.builder()
                    .message(FAILED)
                    .statusCode(throwable.getHttpStatus().value())
                    .status(throwable.getHttpStatus())
                    .data(throwable.getResponse().getData())
                    .errors(throwable.getResponse().getErrors())
                    .build(), throwable.getHttpStatus());        });
    }
}
