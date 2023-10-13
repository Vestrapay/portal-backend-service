package com.example.vestrapay.authentications.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LoginDTO {
    private String email;
    private String password;
}
