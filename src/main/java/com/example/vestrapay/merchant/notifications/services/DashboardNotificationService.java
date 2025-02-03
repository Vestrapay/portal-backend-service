package com.example.vestrapay.merchant.notifications.services;

import com.example.vestrapay.exceptions.CustomException;
import com.example.vestrapay.merchant.authentications.services.AuthenticationService;
import com.example.vestrapay.merchant.notifications.models.Notification;
import com.example.vestrapay.merchant.notifications.repository.NotificationRepository;
import com.example.vestrapay.merchant.users.enums.UserType;
import com.example.vestrapay.utils.dtos.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.example.vestrapay.utils.dtos.Constants.FAILED;
import static com.example.vestrapay.utils.dtos.Constants.SUCCESSFUL;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardNotificationService {
    private final AuthenticationService authenticationService;
    private final NotificationRepository notificationRepository;


    public Mono<Response<List<Notification>>> getTop10Notification(){
        return authenticationService.getLoggedInUser()
                .flatMap(user -> {
                    if (user.getUserType().equals(UserType.SUPER_ADMIN)){
                        return notificationRepository.findTopTen().collectList()
                                .flatMap(notifications -> {
                                    return Mono.just(Response.<List<Notification>>builder()
                                            .data(notifications)
                                            .message(SUCCESSFUL)
                                            .statusCode(HttpStatus.OK.value())
                                            .status(HttpStatus.OK)
                                            .build());

                                }).doOnError(throwable -> {
                                    log.info("error fetching top 10 notifications");
                                    throw new CustomException(Response.<Void>builder()
                                            .message(FAILED)
                                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                            .errors(List.of("error creating account for user",throwable.getLocalizedMessage(),throwable.getMessage()))
                                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                                });
                    }
                    else {
                        return notificationRepository.findTopTenByMerchantId(user.getMerchantId()).collectList()
                                .flatMap(notifications -> {
                                    return Mono.just(Response.<List<Notification>>builder()
                                            .data(notifications)
                                            .message(SUCCESSFUL)
                                            .statusCode(HttpStatus.OK.value())
                                            .status(HttpStatus.OK)
                                            .build());

                                }).doOnError(throwable -> {
                                    log.info("error fetching top 10 notifications");
                                    throw new CustomException(Response.<Void>builder()
                                            .message(FAILED)
                                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                            .errors(List.of("error creating account for user",throwable.getLocalizedMessage(),throwable.getMessage()))
                                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
                                });

                    }
                });

    }

}
