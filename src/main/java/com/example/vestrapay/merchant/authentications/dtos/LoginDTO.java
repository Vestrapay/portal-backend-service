package com.example.vestrapay.merchant.authentications.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LoginDTO {
    private String email;
    private String password;
    @NotBlank(message="OTP must not be null")
    private String otp;
}
