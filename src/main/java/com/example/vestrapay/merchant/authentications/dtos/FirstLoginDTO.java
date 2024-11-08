package com.example.vestrapay.merchant.authentications.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FirstLoginDTO {
    private String email;
    private String password;
}
