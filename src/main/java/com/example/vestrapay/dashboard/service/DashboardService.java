package com.example.vestrapay.dashboard.service;

import com.example.vestrapay.authentications.services.AuthenticationService;
import com.example.vestrapay.dashboard.StatisticsDTO;
import com.example.vestrapay.exceptions.CustomException;
import com.example.vestrapay.notifications.repository.NotificationRepository;
import com.example.vestrapay.notifications.services.NotificationService;
import com.example.vestrapay.transactions.reporitory.TransactionRepository;
import com.example.vestrapay.transactions.services.TransactionService;
import com.example.vestrapay.users.models.User;
import com.example.vestrapay.users.repository.UserRepository;
import com.example.vestrapay.users.services.UserService;
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
import java.util.Map;

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
    public Mono<Response<StatisticsDTO>> getDashboardStatistics(){
        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
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
                                                        return Mono.just(Response.<StatisticsDTO>builder()
                                                                .data(StatisticsDTO.builder()
                                                                        .loggedIssues("0")
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
