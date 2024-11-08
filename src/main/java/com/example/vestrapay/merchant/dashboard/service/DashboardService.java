package com.example.vestrapay.merchant.dashboard.service;

import com.example.vestrapay.merchant.authentications.services.AuthenticationService;
import com.example.vestrapay.merchant.dashboard.StatisticsDTO;
import com.example.vestrapay.exceptions.CustomException;
import com.example.vestrapay.merchant.dispute.enums.DisputeEnum;
import com.example.vestrapay.merchant.dispute.repository.DisputeRepository;
import com.example.vestrapay.merchant.users.enums.UserType;
import com.example.vestrapay.merchant.users.repository.UserRepository;
import com.example.vestrapay.merchant.notifications.repository.NotificationRepository;
import com.example.vestrapay.merchant.transactions.reporitory.TransactionRepository;
import com.example.vestrapay.utils.dtos.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static com.example.vestrapay.utils.dtos.Constants.FAILED;
import static com.example.vestrapay.utils.dtos.Constants.SUCCESSFUL;

@Component
@Slf4j
@RequiredArgsConstructor
public class DashboardService {
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final TransactionRepository transactionRepository;
    private final NotificationRepository notificationRepository;
    private final DisputeRepository disputeRepository;
    public Mono<Response<StatisticsDTO>> getDashboardStatistics(){
        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    if (user.getUserType().equals(UserType.ADMIN)||user.getUserType().equals(UserType.SUPER_ADMIN)){
                        return userRepository.findAll()
                                .collectList()
                                .flatMap(users -> {
                                    LocalDateTime currentDateTime = LocalDateTime.now();

                                    // Get the LocalDateTime for the beginning of the current day (12:00 AM)
                                    LocalDateTime beginningOfDay = currentDateTime.toLocalDate().atTime(LocalTime.MIDNIGHT);
                                    return transactionRepository.findTransactionsByCreatedAtBetween(beginningOfDay,currentDateTime)
                                            .count()
                                            .flatMap(aLong -> {
                                                return notificationRepository.findAllByCreatedAtBetween(beginningOfDay,currentDateTime)
                                                        .count()
                                                        .flatMap(dailyNotifications -> {
                                                            return disputeRepository.findByStatusOrStatus(DisputeEnum.OPENED,DisputeEnum.PENDING)
                                                                    .collectList()
                                                                    .flatMap(disputes -> {
                                                                        return Mono.just(Response.<StatisticsDTO>builder()
                                                                                .data(StatisticsDTO.builder()
                                                                                        .loggedIssues(String.valueOf(disputes.size()))
                                                                                        .recentTransactions(String.valueOf(aLong))
                                                                                        .recentNotifications(String.valueOf(dailyNotifications))
                                                                                        .systemUsers(String.valueOf(users.size()))
                                                                                        .build())
                                                                                .statusCode(HttpStatus.OK.value())
                                                                                .status(HttpStatus.OK)
                                                                                .message(SUCCESSFUL)
                                                                                .build()).cache(Duration.ofMinutes(5));
                                                                    });

                                                        });

                                            });

                                }).doOnError(throwable -> {
                                    throw new CustomException(Response.<StatisticsDTO>builder()
                                            .message(FAILED)
                                            .data(StatisticsDTO.builder()
                                                    .loggedIssues("0")
                                                    .recentTransactions("0")
                                                    .recentNotifications("0")
                                                    .systemUsers("0")
                                                    .build())
                                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                            .errors(List.of("error fetching logged in user",throwable.getLocalizedMessage(),throwable.getMessage()))
                                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                                });
                    }
                    return userRepository.findByMerchantId(user.getMerchantId())
                            .collectList()
                            .flatMap(users -> {
                                LocalDateTime currentDateTime = LocalDateTime.now();

                                // Get the LocalDateTime for the beginning of the current day (12:00 AM)
                                LocalDateTime beginningOfDay = currentDateTime.toLocalDate().atTime(LocalTime.MIDNIGHT);
                                return transactionRepository.findTransactionsByMerchantIdAndCreatedAtBetween(user.getMerchantId(),beginningOfDay,currentDateTime)
                                        .count()
                                        .flatMap(aLong -> {
                                            return notificationRepository.findAllByCreatedAtBetween(beginningOfDay,currentDateTime)
                                                    .count()
                                                    .flatMap(dailyNotifications -> {
                                                        return disputeRepository.findByMerchantIdAndStatusOrStatus(user.getMerchantId(), DisputeEnum.OPENED,DisputeEnum.PENDING)
                                                                .collectList()
                                                                .flatMap(disputes -> {
                                                                    return Mono.just(Response.<StatisticsDTO>builder()
                                                                            .data(StatisticsDTO.builder()
                                                                                    .loggedIssues(String.valueOf(disputes.size()))
                                                                                    .recentTransactions(String.valueOf(aLong))
                                                                                    .recentNotifications(String.valueOf(dailyNotifications))
                                                                                    .systemUsers(String.valueOf(users.size()))
                                                                                    .build())
                                                                            .statusCode(HttpStatus.OK.value())
                                                                            .status(HttpStatus.OK)
                                                                            .message(SUCCESSFUL)
                                                                            .build()).cache(Duration.ofMinutes(5));
                                                                });

                                                    });

                                        });

                            }).doOnError(throwable -> {
                                throw new CustomException(Response.<StatisticsDTO>builder()
                                        .message(FAILED)
                                        .data(StatisticsDTO.builder()
                                                .loggedIssues("0")
                                                .recentTransactions("0")
                                                .recentNotifications("0")
                                                .systemUsers("0")
                                                .build())
                                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .errors(List.of("error fetching logged in user",throwable.getLocalizedMessage(),throwable.getMessage()))
                                        .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                            });



                }).switchIfEmpty(Mono.defer(() -> {
                    log.error("user not logged in or exist");
                    return Mono.just(Response.<StatisticsDTO>builder()
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
