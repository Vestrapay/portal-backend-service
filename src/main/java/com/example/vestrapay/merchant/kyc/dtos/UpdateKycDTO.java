package com.example.vestrapay.merchant.kyc.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateKycDTO {
    private String userId;
    private Boolean status;
}
