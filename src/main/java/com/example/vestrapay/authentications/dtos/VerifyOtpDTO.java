package com.example.vestrapay.authentications.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class VerifyOtpDTO {
    private String email;
    private String otp;
}
