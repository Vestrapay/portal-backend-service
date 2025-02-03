package com.example.vestrapay.merchant.wallet.reporitory;

import com.example.vestrapay.merchant.wallet.model.Wallet;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface WalletRepository extends R2dbcRepository<Wallet,Long> {
    Mono<Wallet>findByWalletId(String walletId);

}
