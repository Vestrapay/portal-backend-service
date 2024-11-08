package com.example.vestrapay.merchant.transactions.dtos;

import com.example.vestrapay.merchant.transactions.enums.PaymentTypeEnum;
import com.example.vestrapay.merchant.transactions.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionSearchFilter {
    private String uuid;
    private String paymentType;
    private String transactionReference;
    private String vestraPayReference;
    private String providerReference;
    private String transactionStatus;
    private String userId;
    private String merchantId;
    private String settlementStatus;
    private String providerName;

    private Integer pageNumber;
    private Integer pageSize;

}
