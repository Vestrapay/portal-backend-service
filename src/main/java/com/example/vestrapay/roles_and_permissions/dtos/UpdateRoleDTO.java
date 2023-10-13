package com.example.vestrapay.roles_and_permissions.dtos;

import com.example.vestrapay.roles_and_permissions.models.Permissions;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpdateRoleDTO {
    private String userId;
    private Permissions permissions;
}
