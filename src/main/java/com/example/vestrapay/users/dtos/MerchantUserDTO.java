package com.example.vestrapay.users.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MerchantUserDTO {
    @NotBlank(message = "country must be provided")
    private String country;
    @NotBlank(message = "firstname must be provided")
    private String firstName;
    @NotBlank(message = "lastname must be provided")
    private String lastName;
    @NotBlank(message = "email must be provided")
    @Email(message = "requires a valid email address")
    private String email;
    private String gender;
    private String phoneNumber;
    @NotBlank(message = "password must be provided")
    private String password;
}
