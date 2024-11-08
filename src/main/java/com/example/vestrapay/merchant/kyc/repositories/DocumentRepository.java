package com.example.vestrapay.merchant.kyc.repositories;

import com.example.vestrapay.merchant.kyc.models.Document;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface DocumentRepository extends R2dbcRepository<Document,Long> {
    Flux<Document>findByMerchantId(String merchantId);
}
