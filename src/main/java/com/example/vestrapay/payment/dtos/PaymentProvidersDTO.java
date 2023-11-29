package com.example.vestrapay.payment.dtos;

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
