package com.example.vestrapay.superadmin.payment.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class RouteRuleDTO {
    private String paymentMethod;
    private String provider;
    private String currency;
}
