package com.example.vestrapay.merchant.keys.service;

import com.example.vestrapay.merchant.authentications.interfaces.IAuthenticationService;
import com.example.vestrapay.exceptions.CustomException;
import com.example.vestrapay.merchant.keys.enums.KeyUsage;
import com.example.vestrapay.merchant.keys.interfaces.IKeyService;
import com.example.vestrapay.merchant.keys.models.Keys;
import com.example.vestrapay.merchant.keys.repository.KeysRepository;
import com.example.vestrapay.merchant.users.models.User;
import com.example.vestrapay.utils.dtos.Response;
import com.example.vestrapay.utils.service.KeyUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
public class KeyService implements IKeyService {
    private final KeysRepository keysRepository;
    private final IAuthenticationService authenticationService;
    private final KeyUtility keyUtility;

    @Value("${test.key.prefix}")
    String testPrefix;

    @Value("${live.key.prefix}")
    String livePrefix;

    @Value("${server.environment}")
    String environment;

    @Override
    public Mono<Response<Keys>> generateKey(KeyUsage keyUsage1) {

        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    KeyUsage keyUsage;
                    if (environment.equalsIgnoreCase("TEST"))
                        keyUsage = KeyUsage.TEST;
                    else
                        keyUsage = KeyUsage.LIVE;

                    Keys keys;
                    if (keyUsage.equals(KeyUsage.TEST)){
                        keys = Keys.builder()
                                .publicKey(testPrefix.concat("PubK").concat(keyUtility.generateHexKey()))
                                .secretKey(testPrefix.concat("PrvK").concat(keyUtility.generateHexKey()))
                                .encryptionKey(testPrefix.concat("encK").concat(keyUtility.generateHexKey()))
                                .build();
                    }
                    else {
                        keys = Keys.builder()
                                .publicKey(livePrefix.concat("PubK").concat(keyUtility.generateHexKey()))
                                .secretKey(livePrefix.concat("PrvK").concat(keyUtility.generateHexKey()))
                                .encryptionKey(livePrefix.concat("encK").concat(keyUtility.generateHexKey()))
                                .build();
                    }

                    keys.setKeyUsage(keyUsage);
                    keys.setUserId(user.getMerchantId());

                    return keysRepository.findByUserIdAndKeyUsage(user.getUuid(), keyUsage.name())
                            .flatMap(keys1 -> {
                                keys1.setEncryptionKey(keys.getEncryptionKey());
                                keys1.setPublicKey(keys.getPublicKey());
                                keys1.setSecretKey(keys.getSecretKey());
                                return keysRepository.save(keys1)
                                        .flatMap(keys2 -> {
                                            log.info("{} keys successfully generated",keyUsage.name());
                                            return Mono.just(Response.<Keys>builder()
                                                            .data(keys1)
                                                            .message(SUCCESSFUL)
                                                            .status(HttpStatus.CREATED)
                                                            .statusCode(HttpStatus.CREATED.value())
                                                    .build());
                                        }).doOnError(throwable -> {
                                            log.error("error saving {} keys, error is {}",keyUsage.name(),throwable.getLocalizedMessage());
                                            throw new CustomException(Response.<User>builder()
                                                    .message(FAILED)
                                                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                    .errors(List.of("error saving keys",throwable.getLocalizedMessage(),throwable.getMessage()))
                                                    .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                                        });

                            })
                            .switchIfEmpty(Mono.defer(() -> {
                                log.info("creating new {} keys ",keyUsage.name());
                                keys.setUuid(UUID.randomUUID().toString());
                                return keysRepository.save(keys)
                                        .flatMap(keys2 -> {
                                            return Mono.just(Response.<Keys>builder()
                                                    .data(keys2)
                                                    .message(SUCCESSFUL)
                                                    .status(HttpStatus.CREATED)
                                                    .statusCode(HttpStatus.CREATED.value())
                                                    .build());
                                        }).doOnError(throwable -> {
                                            log.error("error saving {} keys, error is {}",keyUsage.name(),throwable.getLocalizedMessage());
                                            throw new CustomException(Response.<User>builder()
                                                    .message(FAILED)
                                                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                    .errors(List.of("error saving keys",throwable.getLocalizedMessage(),throwable.getMessage()))
                                                    .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                                        });
                            }))
                            .doOnError(throwable -> {
                                log.error("error fetching {} keys, error is {}",keyUsage.name(),throwable.getLocalizedMessage());
                                throw new CustomException(Response.<User>builder()
                                        .message(FAILED)
                                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .errors(List.of("error fetching keys",throwable.getLocalizedMessage(),throwable.getMessage()))
                                        .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                            });
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("user not logged in");
                    return Mono.just(Response.<Keys>builder()
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .status(HttpStatus.NOT_FOUND)
                            .message(FAILED)
                            .errors(List.of("User not logged in or registered"))
                            .build());

                }))
                .doOnError(throwable -> {
                    log.error("error getting logged in user {}", throwable.getLocalizedMessage());
                    throw new CustomException(Response.<User>builder()
                            .message(FAILED)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .errors(List.of("error fetching user",throwable.getLocalizedMessage(),throwable.getMessage()))
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @Override
    public Mono<Response<Keys>> viewKeys(KeyUsage keyUsage1) {
        return authenticationService.getLoggedInUser().flatMap(user -> {
            KeyUsage keyUsage;
            if (environment.equalsIgnoreCase("TEST"))
                keyUsage = KeyUsage.TEST;
            else
                keyUsage = KeyUsage.LIVE;
            if (keyUsage.equals(KeyUsage.TEST)){
                return keysRepository.findByUserIdAndKeyUsage(user.getUuid(),KeyUsage.TEST.name())
                        .flatMap(keys -> {
                            log.info("test keys gotten");
                            return Mono.just(Response.<Keys>builder()
                                            .message(SUCCESSFUL)
                                            .status(HttpStatus.OK)
                                            .statusCode(HttpStatus.OK.value())
                                            .data(keys)
                                    .build());
                        }).switchIfEmpty(Mono.defer(() -> {
                            log.error("test keys not found for user {}",user.getEmail());
                            return Mono.just(Response.<Keys>builder()
                                            .errors(List.of("test keys not found. generate keys"))
                                            .statusCode(HttpStatus.NOT_FOUND.value())
                                            .status(HttpStatus.NOT_FOUND)
                                            .message(FAILED)
                                    .build());
                        })).doOnError(throwable -> {
                            log.error("error getting test keys for user {}", throwable.getLocalizedMessage());
                            throw new CustomException(Response.<User>builder()
                                    .message(FAILED)
                                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .errors(List.of("error fetching test keys",throwable.getLocalizedMessage(),throwable.getMessage()))
                                    .build(), HttpStatus.INTERNAL_SERVER_ERROR);

                        });
            }
            else
                return keysRepository.findByUserIdAndKeyUsage(user.getUuid(),KeyUsage.LIVE.name())
                        .flatMap(keys -> {
                            return Mono.just(Response.<Keys>builder()
                                    .message(SUCCESSFUL)
                                    .status(HttpStatus.OK)
                                    .statusCode(HttpStatus.OK.value())
                                    .data(keys)
                                    .build());
                        }).switchIfEmpty(Mono.defer(() -> {
                            log.error("live keys not found for user {}",user.getEmail());
                            return Mono.just(Response.<Keys>builder()
                                    .errors(List.of("live keys not found. generate keys"))
                                    .statusCode(HttpStatus.NOT_FOUND.value())
                                    .status(HttpStatus.NOT_FOUND)
                                    .message(FAILED)
                                    .build());
                        })).doOnError(throwable -> {
                            log.error("error getting live keys for user {}", throwable.getLocalizedMessage());
                            throw new CustomException(Response.<User>builder()
                                    .message(FAILED)
                                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .errors(List.of("error fetching test keys",throwable.getLocalizedMessage(),throwable.getMessage()))
                                    .build(), HttpStatus.INTERNAL_SERVER_ERROR);

                        });

        }).switchIfEmpty(Mono.defer(() -> {
            log.error("user not logged in");
            return Mono.just(Response.<Keys>builder()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .status(HttpStatus.NOT_FOUND)
                    .message(FAILED)
                    .errors(List.of("User not logged in or registered"))
                    .build());

        })).doOnError(throwable -> {
            log.error("error getting logged in user {}", throwable.getLocalizedMessage());
            throw new CustomException(Response.<User>builder()
                    .message(FAILED)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .errors(List.of("error fetching user",throwable.getLocalizedMessage(),throwable.getMessage()))
                    .build(), HttpStatus.INTERNAL_SERVER_ERROR);

        });
    }

    @Override
    public Mono<Response<Keys>> adminGenerateProdKeys(KeyUsage keyUsage, String merchantId) {
        Keys prodKeys = Keys.builder()
                .publicKey(livePrefix.concat("PubK").concat(keyUtility.generateHexKey()))
                .secretKey(livePrefix.concat("PrvK").concat(keyUtility.generateHexKey()))
                .encryptionKey(livePrefix.concat("encK").concat(keyUtility.generateHexKey()))
                .build();

        prodKeys.setKeyUsage(keyUsage);
        prodKeys.setUserId(merchantId);

        return keysRepository.findByUserIdAndKeyUsage(merchantId, keyUsage.name())
                .flatMap(keys1 -> {
                    keys1.setEncryptionKey(prodKeys.getEncryptionKey());
                    keys1.setPublicKey(prodKeys.getPublicKey());
                    keys1.setSecretKey(prodKeys.getSecretKey());
                    return keysRepository.save(keys1)
                            .flatMap(keys2 -> {
                                log.info("{} keys successfully generated",keyUsage.name());
                                return Mono.just(Response.<Keys>builder()
                                        .data(keys1)
                                        .message(SUCCESSFUL)
                                        .status(HttpStatus.CREATED)
                                        .statusCode(HttpStatus.CREATED.value())
                                        .build());
                            }).doOnError(throwable -> {
                                log.error("error saving {} keys, error is {}",keyUsage.name(),throwable.getLocalizedMessage());
                                throw new CustomException(Response.<User>builder()
                                        .message(FAILED)
                                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .errors(List.of("error saving keys",throwable.getLocalizedMessage(),throwable.getMessage()))
                                        .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                            });

                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("creating new {} keys ",keyUsage.name());
                    prodKeys.setUuid(UUID.randomUUID().toString());
                    return keysRepository.save(prodKeys)
                            .flatMap(keys2 -> {
                                return Mono.just(Response.<Keys>builder()
                                        .data(keys2)
                                        .message(SUCCESSFUL)
                                        .status(HttpStatus.CREATED)
                                        .statusCode(HttpStatus.CREATED.value())
                                        .build());
                            }).doOnError(throwable -> {
                                log.error("error saving {} keys, error is {}",keyUsage.name(),throwable.getLocalizedMessage());
                                throw new CustomException(Response.<User>builder()
                                        .message(FAILED)
                                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .errors(List.of("error saving keys",throwable.getLocalizedMessage(),throwable.getMessage()))
                                        .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                            });
                }))
                .doOnError(throwable -> {
                    log.error("error fetching {} keys, error is {}",keyUsage.name(),throwable.getLocalizedMessage());
                    throw new CustomException(Response.<User>builder()
                            .message(FAILED)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .errors(List.of("error fetching keys",throwable.getLocalizedMessage(),throwable.getMessage()))
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

}
