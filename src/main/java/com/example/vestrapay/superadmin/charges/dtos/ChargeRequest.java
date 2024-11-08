package com.example.vestrapay.superadmin.charges.dtos;

import com.example.vestrapay.superadmin.charges.enums.ChargeCategory;
import com.example.vestrapay.superadmin.charges.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ChargeRequest {
    @NotBlank
    private String merchantId;
    private PaymentMethod paymentMethod;
    private ChargeCategory category;
    private BigDecimal percentage;
    private BigDecimal floor = BigDecimal.ZERO;
    private BigDecimal cap;
    private BigDecimal flatFee;
    private boolean useFlatFee;
}
