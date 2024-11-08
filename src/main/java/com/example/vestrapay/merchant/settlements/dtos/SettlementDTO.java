package com.example.vestrapay.merchant.settlements.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SettlementDTO {
    private String country;
    private String currency;
    private String bankName;
    private String accountNumber;
}
