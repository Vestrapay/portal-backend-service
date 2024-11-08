package com.example.vestrapay.superadmin.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class AdminUserDTO {
    @NotBlank(message = "firstname must be provided")
    private String firstName;
    @NotBlank(message = "lastname must be provided")
    private String lastName;
    @NotBlank(message = "email must be provided")
    @Email(message = "requires a valid email address")
    private String email;
    private String country;
    @NotEmpty(message = "phone number must be provided")
    private String phoneNumber;
    @NotBlank(message = "password must be provided")
    private String password;
}
