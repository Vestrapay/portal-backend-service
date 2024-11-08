package com.example.vestrapay.merchant.users.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMerchantUserDTO {
    private String country;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private boolean enabled;

}
