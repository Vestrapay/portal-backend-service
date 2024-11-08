package com.example.vestrapay.merchant.dashboard.controller;

import com.example.vestrapay.merchant.dashboard.StatisticsDTO;
import com.example.vestrapay.merchant.dashboard.models.Balance;
import com.example.vestrapay.merchant.dashboard.service.DashboardService;
import com.example.vestrapay.merchant.notifications.models.Notification;
import com.example.vestrapay.merchant.notifications.services.DashboardNotificationService;
import com.example.vestrapay.merchant.notifications.services.NotificationService;
import com.example.vestrapay.merchant.transactions.interfaces.ITransactionService;
import com.example.vestrapay.merchant.transactions.models.Transaction;
import com.example.vestrapay.utils.dtos.Response;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v1/dashboard")
@Tag(name = "DASHBOARD", description = "Dashboard Management")
@SecurityRequirement(name = "vestrapay")
@CrossOrigin(origins ="*",maxAge = 3600)
@RequiredArgsConstructor
public class DashboardController {
    private final NotificationService notificationService;
    private final ITransactionService transactionService;
    private final DashboardNotificationService dashboardNotificationService;
    private final DashboardService dashboardService;
    @GetMapping("get-recent-transactions")
    public Mono<ResponseEntity<Response<List<Transaction>>>> getTop10Tranasctions(){
        return transactionService.getTop10TransactionForUser()
                .map(listResponse -> ResponseEntity.status(listResponse.getStatus()).body(listResponse));
    }

    @PostMapping("analytics")
    public Mono<ResponseEntity<Response<List<Transaction>>>> getTransactionAnalytics(@RequestBody Map<String,Object > request){

        return transactionService.getDailyTransactions()
                .map(listResponse -> ResponseEntity.status(listResponse.getStatus()).body(listResponse));    }

    @GetMapping("notification")
    public Mono<ResponseEntity<Response<List<Notification>>>> getTopTenNotifications(){
        return dashboardNotificationService.getTop10Notification()
                .map(listResponse -> ResponseEntity.status(listResponse.getStatus()).body(listResponse));
    }

    @GetMapping("statistics")
    public Mono<ResponseEntity<Response<StatisticsDTO>>> getStatistics(){

        return dashboardService.getDashboardStatistics()
                .map(mapResponse -> ResponseEntity.status(mapResponse.getStatus()).body(mapResponse));
    }

    @GetMapping("balance")
    public Mono<ResponseEntity<Response<Balance>>> getBalances(){
        return transactionService.getUnSettledTransactions()
                .map(balanceResponse -> ResponseEntity.status(balanceResponse.getStatus()).body(balanceResponse));
    }

    @RequestMapping(
            value = "/**",
            method = RequestMethod.OPTIONS
    )
    public ResponseEntity handle() {
        return new ResponseEntity(HttpStatus.OK);
    }
}
