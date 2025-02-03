package com.example.vestrapay.merchant.authentications.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ChangePasswordDTO {
    @NotBlank(message = "old password must be provided")
    private String oldPassword;
    @NotBlank(message = "new password must be provided")
    private String newPassword;
}
