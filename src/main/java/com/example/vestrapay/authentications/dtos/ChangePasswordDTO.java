package com.example.vestrapay.authentications.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ChangePasswordDTO {
    private String oldPassword;
    private String newPassword;
}
