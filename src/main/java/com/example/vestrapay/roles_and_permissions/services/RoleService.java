package com.example.vestrapay.roles_and_permissions.services;

import com.example.vestrapay.authentications.services.AuthenticationService;
import com.example.vestrapay.exceptions.CustomException;
import com.example.vestrapay.roles_and_permissions.dtos.RoleDTO;
import com.example.vestrapay.roles_and_permissions.dtos.UpdateRoleDTO;
import com.example.vestrapay.roles_and_permissions.interfaces.IPermissionService;
import com.example.vestrapay.roles_and_permissions.interfaces.IRoleService;
import com.example.vestrapay.roles_and_permissions.models.RolePermission;
import com.example.vestrapay.roles_and_permissions.repository.RolePermissionRepository;
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
import static com.example.vestrapay.utils.dtos.Constants.SUCCESSFUL;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoleService implements IRoleService {
    private final RolePermissionRepository rolePermissionRepository;
    private final AuthenticationService authenticationService;
    private final IPermissionService permissionService;
    @Override
    public Mono<Response<List<RolePermission>>> createRole(RoleDTO request) {
        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    log.info("user gotten for create role");
                    return rolePermissionRepository.findFirstByUserId(request.getUserId())
                            .flatMap(rolePermission -> {
                                log.info("role already exists for user. update role to make changes");
                                return Mono.just(Response.<List<RolePermission>>builder()
                                                .statusCode(HttpStatus.CONFLICT.value())
                                                .status(HttpStatus.CONFLICT)
                                                .message(FAILED)
                                                .errors(List.of("role already exists for user. update role to make changes"))
                                        .build());
                            }).switchIfEmpty(Mono.defer(() -> {
                                request.getPermissions().forEach(permissions -> rolePermissionRepository.save(RolePermission.builder()
                                                .merchantID(user.getMerchantId())
                                                .permissionId(permissions.getPermissionName())
                                                .uuid(UUID.randomUUID().toString())
                                                .userId(request.getUserId())
                                        .build()).subscribe());

                                return rolePermissionRepository.findAll().collectSortedList().flatMap(rolePermissions -> Mono.just(Response.<List<RolePermission>>builder()
                                                .data(rolePermissions)
                                                .message(SUCCESSFUL)
                                                .status(HttpStatus.CREATED)
                                                .statusCode(HttpStatus.CREATED.value())
                                        .build()));

                            })).doOnError(throwable -> {
                                log.error("error checking for user roles on db. error is {}",throwable.getLocalizedMessage());
                                throw new CustomException(Response.builder()
                                        .message(FAILED)
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .errors(List.of("error checking for user roles on db",throwable.getLocalizedMessage()))
                                        .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                            });
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("user not found");
                    return Mono.just(Response.<List<RolePermission>>builder()
                            .errors(List.of("user not found or logged in or token expired"))
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .status(HttpStatus.NOT_FOUND)
                            .message(FAILED)
                            .build());
                }))
                .doOnError(throwable -> {
                    log.error("error fetching logged in user. error is {}",throwable.getLocalizedMessage());
                    throw new CustomException(Response.builder()
                            .message(FAILED)
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .errors(List.of("error fetching logged in user",throwable.getLocalizedMessage()))
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }
    @Override
    public Mono<Void> createDefaultRole(String userId,String merchantId,String userType) {
        Mono<Boolean> processMono = Mono.fromCallable(() -> {
            if (userType.equalsIgnoreCase("MERCHANT")){
                createMerchantRole(userId,merchantId).subscribe();
            }
            else {
                createMerchantUser(userId,merchantId).subscribe();
            }
            return true;
        });
        processMono.subscribeOn(Schedulers.boundedElastic()).subscribe();
        return Mono.empty();    }
    @Override
    public Mono<Response<RolePermission>> addPermissionToUser(UpdateRoleDTO request) {
        log.info("about adding permission to user with DTO {}",request.toString());
        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    log.info("user logged in. about adding permission");
                    //only merchants can add or remove roles and permissions
                    return rolePermissionRepository.findByUserIdAndMerchantIDAndPermissionId(request.getUserId(), user.getMerchantId(), request.getPermissions().getPermissionName())
                            .flatMap(rolePermission -> {
                                log.error("permission already exist for user");
                                return Mono.just(Response.<RolePermission>builder()
                                                .errors(List.of("Permission already exist for user"))
                                                .statusCode(HttpStatus.CONFLICT.value())
                                                .status(HttpStatus.CONFLICT)
                                                .message(FAILED)
                                        .build());

                            }).switchIfEmpty(Mono.defer(() -> {
                                log.error("creating permission with DTO {}",request.toString());
                                return rolePermissionRepository.save(RolePermission.builder()
                                                .uuid(UUID.randomUUID().toString())
                                                .merchantID(user.getMerchantId())
                                                .userId(request.getUserId())
                                                .permissionId(request.getPermissions().getPermissionName())
                                                .build())
                                        .flatMap(rolePermission -> {
                                            log.info("user permission created successfully");
                                            return Mono.just(Response.<RolePermission>builder()
                                                            .message(SUCCESSFUL)
                                                            .status(HttpStatus.OK)
                                                            .statusCode(HttpStatus.OK.value())
                                                            .data(rolePermission)
                                                    .build());
                                        }).doOnError(throwable -> {
                                            log.error("error saving role permission. error is {}",throwable.getLocalizedMessage());
                                            throw new CustomException(Response.builder()
                                                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                                    .message(FAILED)
                                                    .errors(List.of("error saving user role permission.",throwable.getLocalizedMessage()))
                                                    .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                                        });

                            }));
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("user not logged in or exist");
                    return Mono.just(Response.<RolePermission>builder()
                            .errors(List.of("user not logged in"))
                                    .message(FAILED)
                                    .statusCode(HttpStatus.NOT_FOUND.value())
                                    .status(HttpStatus.NOT_FOUND)
                            .build());
                }))
                .doOnError(throwable -> {
                    log.error("error fetching logged in user. error is {}",throwable.getLocalizedMessage());
                    throw new CustomException(Response.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message(FAILED)
                            .errors(List.of("error fetching logged in user.",throwable.getLocalizedMessage()))
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }
    @Override
    public Mono<Response<Void>> removePermissionFromUser(UpdateRoleDTO request) {
        log.info("about adding permission to user with DTO {}",request.toString());
        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    return rolePermissionRepository.findByUserIdAndMerchantIDAndPermissionId(user.getMerchantId(), request.getUserId(),request.getPermissions().getPermissionName())
                            .flatMap(rolePermission -> {
                                log.info("role permission found");
                                return rolePermissionRepository.delete(rolePermission)
                                        .flatMap(unused -> {
                                            return Mono.just(Response.<Void>builder()
                                                            .message(SUCCESSFUL)
                                                            .statusCode(HttpStatus.NO_CONTENT.value())
                                                            .status(HttpStatus.NO_CONTENT)
                                                    .build());
                                        });

                            }).switchIfEmpty(Mono.defer(() -> {
                                log.error("role permission not found");
                                return Mono.just(Response.<Void>builder()
                                        .errors(List.of("user not logged in"))
                                        .message(FAILED)
                                        .statusCode(HttpStatus.NOT_FOUND.value())
                                        .status(HttpStatus.NOT_FOUND)
                                        .build());

                            }))
                            .doOnError(throwable -> {
                                throw new CustomException(Response.builder()
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .message(FAILED)
                                        .errors(List.of("error fetching rolePermission to be deleted.",throwable.getLocalizedMessage()))
                                        .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                            });

                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("user not logged in or exist");
                    return Mono.just(Response.<Void>builder()
                            .errors(List.of("user not logged in"))
                            .message(FAILED)
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .status(HttpStatus.NOT_FOUND)
                            .build());
                }))
                .doOnError(throwable -> {
                    log.error("error fetching logged in user. error is {}",throwable.getLocalizedMessage());
                    throw new CustomException(Response.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message(FAILED)
                            .errors(List.of("error fetching logged in user.",throwable.getLocalizedMessage()))
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }
    @Override
    public Mono<Response<RolePermission>> viewUserRole(String userId) {
        return null;
    }

    private Mono<Response<Boolean>> createMerchantRole(String userId,String merchantId){
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
}
