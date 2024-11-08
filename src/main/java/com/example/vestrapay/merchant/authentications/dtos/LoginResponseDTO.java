package com.example.vestrapay.merchant.authentications.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginResponseDTO {
    private boolean enabled;
    private Object user;

}
