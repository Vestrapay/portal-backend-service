package com.example.vestrapay.settlements.interfaces;

import com.example.vestrapay.settlements.dtos.SettlementDTO;
import com.example.vestrapay.settlements.models.Settlement;
import com.example.vestrapay.settlements.models.WemaAccounts;
import com.example.vestrapay.users.models.User;
import com.example.vestrapay.utils.dtos.Response;
import io.r2dbc.spi.Result;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ISettlementService {
    Mono<Response<Settlement>> addAccount(SettlementDTO request);
    Mono<Response<WemaAccounts>> generateWemaAccountForMerchant(User request);
    Mono<Response<WemaAccounts>> viewWemaAccount();
    Mono<Response<Settlement>> updateAccount(Settlement request);
    Mono<Response<Void>> removeAccount(Settlement request);
    Mono<Response<Void>> setPrimaryAccount(String settlementUUID);

    Mono<Response<List<Settlement>>> viewAllUserAccounts();

    Mono<Response<Settlement>> viewAccount(String uuid);

    Mono<Response<Settlement>> viewPrimaryAccount();
}
