package com.example.vestrapay.roles_and_permissions.dtos;

import com.example.vestrapay.roles_and_permissions.models.Permissions;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RoleDTO {
    private String userId;
    private List<Permissions> permissions;
}
