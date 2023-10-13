package com.example.vestrapay.roles_and_permissions.services;

import com.example.vestrapay.authentications.services.AuthenticationService;
import com.example.vestrapay.exceptions.CustomException;
import com.example.vestrapay.roles_and_permissions.interfaces.IPermissionService;
import com.example.vestrapay.roles_and_permissions.models.Permissions;
import com.example.vestrapay.roles_and_permissions.repository.PermissionRepository;
import com.example.vestrapay.utils.dtos.Response;
import com.example.vestrapay.utils.service.RedisUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.vestrapay.utils.dtos.Constants.FAILED;
import static com.example.vestrapay.utils.dtos.Constants.SUCCESSFUL;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService implements IPermissionService
{
    private final AuthenticationService authenticationService;
    private final RedisUtility redisUtility;
    private final PermissionRepository permissionRepository;
    @Override
    public Mono<Response<List<Permissions>>> getAllPermissions() {
        return authenticationService.getLoggedInUser().flatMap(user -> {
            log.info("logged in user gotten for getAllPermissions");
            return permissionRepository.findAll().collectList()
                    .flatMap(permissions -> {
                        log.info("permissions gotten");
                        return Mono.just(Response.<List<Permissions>>builder()
                                        .data(permissions)
                                        .message(SUCCESSFUL)
                                        .status(HttpStatus.OK)
                                        .statusCode(HttpStatus.OK.value())
                                .build());
                    })
                    .doOnError(throwable -> {
                        log.error("error fetching all permissions from db, error is {}",throwable.getLocalizedMessage());
                        throw new CustomException(Response.builder()
                                .message(FAILED)
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .errors(List.of("error fetching permissions from db",throwable.getLocalizedMessage()))
                                .build(), HttpStatus.INTERNAL_SERVER_ERROR);                    });

        }).switchIfEmpty(Mono.defer(() -> {
            log.error("user not found");
            return Mono.just(Response.<List<Permissions>>builder()
                            .errors(List.of("user not found or logged in or token expired"))
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .status(HttpStatus.NOT_FOUND)
                            .message(FAILED)
                    .build());

        })).doOnError(throwable -> {
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
    public Mono<Response<List<Permissions>>> getAllMerchantUserPermissions() {
        return permissionRepository.findAll().collectList()
                .flatMap(permissions -> {
                    List<Permissions> merchantPermissions = permissions.stream()
                            .filter(permissions1 -> !permissions1.getPermissionName().contains("ADMINS"))
                            .filter(permissions1 -> !permissions1.getPermissionName().contains("ROLES"))
                            .toList();
                    log.info("permissions gotten");
                    return Mono.just(Response.<List<Permissions>>builder()
                            .data(merchantPermissions)
                            .message(SUCCESSFUL)
                            .status(HttpStatus.OK)
                            .statusCode(HttpStatus.OK.value())
                            .build());

        }).switchIfEmpty(Mono.defer(() -> {
            log.error("permissions not found");
            return Mono.just(Response.<List<Permissions>>builder()
                    .errors(List.of("permissions not found or logged in or token expired"))
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .status(HttpStatus.NOT_FOUND)
                    .message(FAILED)
                    .build());

        })).doOnError(throwable -> {
            log.error("error fetching permissions  error is {}",throwable.getLocalizedMessage());
            throw new CustomException(Response.builder()
                    .message(FAILED)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .errors(List.of("error fetching permissions",throwable.getLocalizedMessage()))
                    .build(), HttpStatus.INTERNAL_SERVER_ERROR);
        });
    }
    public Mono<Response<List<Permissions>>> getAllMerchantPermissions() {
        return permissionRepository.findAll().collectList()
                .flatMap(permissions -> {
                    List<Permissions> merchantPermissions = permissions.stream().filter(permissions1 -> !permissions1.getPermissionName().contains("ADMINS"))
                            .toList();
                    log.info("permissions gotten");
                    return Mono.just(Response.<List<Permissions>>builder()
                            .data(merchantPermissions)
                            .message(SUCCESSFUL)
                            .status(HttpStatus.OK)
                            .statusCode(HttpStatus.OK.value())
                            .build());

        }).switchIfEmpty(Mono.defer(() -> {
            log.error("permissions not found");
            return Mono.just(Response.<List<Permissions>>builder()
                    .errors(List.of("permissions not found or logged in or token expired"))
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .status(HttpStatus.NOT_FOUND)
                    .message(FAILED)
                    .build());

        })).doOnError(throwable -> {
            log.error("error fetching permissions  error is {}",throwable.getLocalizedMessage());
            throw new CustomException(Response.builder()
                    .message(FAILED)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .errors(List.of("error fetching permissions",throwable.getLocalizedMessage()))
                    .build(), HttpStatus.INTERNAL_SERVER_ERROR);
        });
    }

}
