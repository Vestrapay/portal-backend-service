package com.example.vestrapay.merchant.users.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class UserUpdateDTO {
    private String country;
    private String firstName;
    private String lastName;
    private String businessName;
    private String referralCode;
    private String phoneNumber;
    private String password;
}
