package com.example.vestrapay.superadmin.users.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EnableDisableDTO {
    private String adminUUID;
    private Boolean value;
}
