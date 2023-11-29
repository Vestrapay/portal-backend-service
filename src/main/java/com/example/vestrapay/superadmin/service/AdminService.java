package com.example.vestrapay.superadmin.service;

import com.example.vestrapay.exceptions.CustomException;
import com.example.vestrapay.users.enums.UserType;
import com.example.vestrapay.users.models.User;
import com.example.vestrapay.users.repository.UserRepository;
import com.example.vestrapay.utils.dtos.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService implements IAdminService {
    private final UserRepository userRepository;
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
    public Mono<Response<Boolean>> disableMerchant(String merchantId) {
        return userRepository.findByMerchantId(merchantId)
                .collectList()
                .flatMap(users -> {
                    users.forEach(user -> {
                        user.setEnabled(false);
                    });

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
                });
    }
}
