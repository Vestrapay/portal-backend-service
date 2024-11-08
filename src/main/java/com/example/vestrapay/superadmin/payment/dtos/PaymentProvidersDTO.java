package com.example.vestrapay.superadmin.payment.dtos;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class PaymentProvidersDTO {

    private String name;
    private Set<String> supportedPaymentMethods;


}
