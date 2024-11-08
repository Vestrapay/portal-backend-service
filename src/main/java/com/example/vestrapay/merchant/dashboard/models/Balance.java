package com.example.vestrapay.merchant.dashboard.models;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class Balance {
    private BigDecimal currentBalance;
    private BigDecimal income;
    private BigDecimal outcome;
}
