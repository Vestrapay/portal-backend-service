package com.example.vestrapay.merchant.transactions.dtos;

import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionRangeDTO {
    private String from;
    private String to;
}
