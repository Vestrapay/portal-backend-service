package com.example.vestrapay.payment.dtos;

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
}
