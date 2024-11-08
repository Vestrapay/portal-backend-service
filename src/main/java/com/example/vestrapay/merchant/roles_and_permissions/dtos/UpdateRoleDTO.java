package com.example.vestrapay.merchant.roles_and_permissions.dtos;

import com.example.vestrapay.merchant.roles_and_permissions.models.Permissions;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpdateRoleDTO {
    private String userId;
    private Permissions permissions;
}
