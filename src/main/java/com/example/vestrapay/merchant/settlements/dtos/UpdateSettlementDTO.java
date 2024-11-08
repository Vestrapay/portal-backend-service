package com.example.vestrapay.merchant.settlements.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateSettlementDTO {
    private String uuid;
    private String country;
    private String merchantId;
    private String currency;
    private String bankName;
    private String accountNumber;

}
