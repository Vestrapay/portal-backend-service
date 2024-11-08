package com.example.vestrapay.merchant.transactions.dtos;

import com.example.vestrapay.merchant.transactions.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRecord {
    private String id;
    private String uuid;
    private String  paymentType;
    private BigDecimal amount;
    private String pan;
    private BigDecimal fee;
    private String transactionReference;
    private String vestraPayReference;
    private String providerReference;
    private String cardScheme;
    private String narration;
    private String activityStatus;
    private String transactionStatus;
    private String userId;
    private String merchantId;
    private String providerName;
    private String settlementStatus;
    private String customerId;
    private String createdAt;


}
