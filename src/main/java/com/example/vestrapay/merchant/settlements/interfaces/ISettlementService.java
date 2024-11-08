package com.example.vestrapay.merchant.settlements.interfaces;

import com.example.vestrapay.merchant.settlements.dtos.SettlementDTO;
import com.example.vestrapay.merchant.settlements.dtos.UpdateSettlementDTO;
import com.example.vestrapay.merchant.settlements.models.WemaAccounts;
import com.example.vestrapay.merchant.settlements.enums.SettlementEnum;
import com.example.vestrapay.merchant.settlements.models.Settlement;
import com.example.vestrapay.utils.dtos.Response;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ISettlementService {
    Mono<Response<Settlement>> addAccount(SettlementDTO request);
    Mono<Response<WemaAccounts>> generateWemaAccountForMerchant();
    Mono<Response<WemaAccounts>> viewWemaAccount();
    Mono<Response<Settlement>> updateAccount(UpdateSettlementDTO request);
    Mono<Response<Void>> removeAccount(Settlement request);
    Mono<Response<Void>> setPrimaryAccount(String settlementUUID);
    Mono<Response<List<Settlement>>> viewAllUserAccounts();
    Mono<Response<Settlement>> viewAccount(String uuid);
    Mono<Response<Settlement>> viewPrimaryAccount();

    Mono<Response<List<SettlementEnum>>> settlementDurations();

}
