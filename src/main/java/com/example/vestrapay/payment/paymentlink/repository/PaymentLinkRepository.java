package com.example.vestrapay.payment.paymentlink.repository;

import com.example.vestrapay.payment.paymentlink.entity.PaymentLink;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface PaymentLinkRepository extends R2dbcRepository<PaymentLink,Long> {
    Mono<PaymentLink> findByMerchantIdAndInvoiceId(String merchantId, String invoiceId);
}
