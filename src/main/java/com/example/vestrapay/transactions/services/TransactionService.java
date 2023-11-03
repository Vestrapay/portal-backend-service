package com.example.vestrapay.transactions.services;

import com.example.vestrapay.authentications.interfaces.IAuthenticationService;
import com.example.vestrapay.exceptions.CustomException;
import com.example.vestrapay.transactions.enums.PaymentTypeEnum;
import com.example.vestrapay.transactions.interfaces.ITransactionService;
import com.example.vestrapay.transactions.models.Transaction;
import com.example.vestrapay.transactions.reporitory.TransactionRepository;
import com.example.vestrapay.users.enums.UserType;
import com.example.vestrapay.utils.dtos.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static com.example.vestrapay.utils.dtos.Constants.FAILED;
import static com.example.vestrapay.utils.dtos.Constants.SUCCESSFUL;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionService implements ITransactionService {
    private final TransactionRepository transactionRepository;
    private final IAuthenticationService authenticationService;
    @Override
    public Mono<Response<Transaction>> getTransactionByParam(Map<String, Object> request) {
        log.info("about fetching one transaction by param {}",request.toString());

        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    log.info("user is logged in");
                    //todo extract necessary parameters
                    return Mono.just(Response.<Transaction>builder().build());

                }).switchIfEmpty(Mono.defer(() -> {
                    log.info("user is not logged in or user does not exist");
                    return Mono.just(Response.<Transaction>builder().build());
                })).doOnError(throwable -> {
                    log.error("error fetching logged in user, error is {}",throwable.getLocalizedMessage());
                    throw new CustomException(Response.<Transaction>builder()
                            .message(FAILED)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .errors(List.of("error fetching logged in user,r",throwable.getLocalizedMessage(),throwable.getMessage()))
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @Override
    public Mono<Response<List<Transaction>>> getTransactionsByParam(Map<String, Object> request) {
        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    if (request.isEmpty()){
                        return transactionRepository.findByMerchantId(user.getMerchantId())
                                .collectList()
                                .flatMap(transactions -> {
                                    return Mono.just(Response.<List<Transaction>>builder()
                                                    .message(SUCCESSFUL)
                                                    .data(transactions)
                                                    .status(HttpStatus.OK)
                                                    .statusCode(HttpStatus.OK.value())
                                            .build());
                                }).doOnError(throwable -> {
                                    log.error("error fetching transactions. error is {}",throwable.getMessage());
                                    throw new CustomException(throwable);
                                });
                    }
                    return Mono.empty();

                });
    }

    @Override
    public Mono<Response<List<Transaction>>> getTopTenTransactions(Map<String, Object> request) {
        return null;
    }

    @Override
    public Mono<Response<List<Transaction>>> adminGetAllTransactions(Map<String, Object> request) {
        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    if (user.getUserType().equals(UserType.SUPER_ADMIN)||user.getUserType().equals(UserType.ADMIN)){
                        if (request.keySet().isEmpty()){
                            return transactionRepository.findAll()
                                    .collectList()
                                    .flatMap(transactions -> {
                                        return Mono.just(Response.<List<Transaction>>builder()
                                                        .data(transactions)
                                                        .message(SUCCESSFUL)
                                                        .statusCode(HttpStatus.OK.value())
                                                        .status(HttpStatus.OK)
                                                .build());
                                    });
                        }
                        else {
                            Transaction transaction = new Transaction();
                            for (String key: request.keySet()) {
                                if (key.equals("id"))
                                    transaction.setId((Long) request.get(key));
                                if (key.equals("uuid"))
                                    transaction.setUuid((String) request.get(key));
                                if (key.equals("paymentType"))
                                    transaction.setPaymentType((PaymentTypeEnum) request.get(key));
                                if (key.equals("transactionReference"))
                                    transaction.setTransactionReference((String) request.get(key));
                                if (key.equals("transactionReference"))
                                    transaction.setTransactionReference((String) request.get(key));
                                if (key.equals("merchantId"))
                                    transaction.setMerchantId((String) request.get(key));
                                if (key.equals("userId"))
                                    transaction.setUserId((String) request.get(key));
                            }

                            return transactionRepository.findAll(Example.of(transaction, ExampleMatcher.matchingAny()))
                                    .collectList()
                                    .flatMap(transactions -> {
                                        return Mono.just(Response.<List<Transaction>>builder()
                                                        .data(transactions)
                                                        .status(HttpStatus.OK)
                                                        .statusCode(HttpStatus.OK.value())
                                                        .message(SUCCESSFUL)
                                                .build());
                                    });

                        }

                    }
                    else {
                        return Mono.just(Response.<List<Transaction>>builder()
                                        .errors(List.of("Permission denied"))
                                        .status(HttpStatus.UNAUTHORIZED)
                                        .statusCode(HttpStatus.UNAUTHORIZED.value())
                                        .message(FAILED)
                                .build());
                    }
                });
    }

    @Override
    public Mono<Response<List<Transaction>>> getTop10TransactionForUser() {
        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    return transactionRepository.findTop10(user.getMerchantId())
                            .collectList().flatMap(transactions -> {
                                return Mono.just(Response.<List<Transaction>>builder()
                                                .message(SUCCESSFUL)
                                                .data(transactions)
                                                .statusCode(HttpStatus.OK.value())
                                                .status(HttpStatus.OK)
                                        .build());
                            });

                }).switchIfEmpty(Mono.defer(() -> {
                    log.error("user not logged in or exist");
                    return Mono.just(Response.<List<Transaction>>builder()
                                    .status(HttpStatus.NOT_FOUND)
                                    .statusCode(HttpStatus.NOT_FOUND.value())
                                    .message(FAILED)
                                    .errors(List.of("user not found or logged in"))
                            .build());
                })).doOnError(throwable -> {
                    log.error("error getting top 10 transactions for user");
                    throw new CustomException(Response.<Transaction>builder()
                            .message(FAILED)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .errors(List.of("error fetching logged in user,r",throwable.getLocalizedMessage(),throwable.getMessage()))
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @Override
    public Mono<Response<List<Transaction>>> getDailyTransactions() {
        log.info("about fetching daily transactions");
        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    LocalDateTime currentDateTime = LocalDateTime.now();

                    // Get the LocalDateTime for the beginning of the current day (12:00 AM)
                    LocalDateTime beginningOfDay = currentDateTime.toLocalDate().atTime(LocalTime.MIDNIGHT);

                    return transactionRepository.findTransactionsByMerchantIdAndCreatedAtBetween(user.getMerchantId(),beginningOfDay ,LocalDateTime.now())
                            .collectList().flatMap(transactions -> {
                                return Mono.just(Response.<List<Transaction>>builder()
                                        .message(SUCCESSFUL)
                                        .data(transactions)
                                        .statusCode(HttpStatus.OK.value())
                                        .status(HttpStatus.OK)
                                        .build());
                            });

                }).switchIfEmpty(Mono.defer(() -> {
                    log.error("user not logged in or exist");
                    return Mono.just(Response.<List<Transaction>>builder()
                            .status(HttpStatus.NOT_FOUND)
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .message(FAILED)
                            .errors(List.of("user not found or logged in"))
                            .build());
                })).doOnError(throwable -> {
                    log.error("error getting top 10 transactions for user");
                    throw new CustomException(Response.<Transaction>builder()
                            .message(FAILED)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .errors(List.of("error fetching logged in user,r",throwable.getLocalizedMessage(),throwable.getMessage()))
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                });    }
}
