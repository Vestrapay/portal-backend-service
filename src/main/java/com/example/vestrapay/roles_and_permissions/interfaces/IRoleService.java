package com.example.vestrapay.roles_and_permissions.interfaces;

import com.example.vestrapay.roles_and_permissions.dtos.RoleDTO;
import com.example.vestrapay.roles_and_permissions.dtos.UpdateRoleDTO;
import com.example.vestrapay.roles_and_permissions.models.RolePermission;
import com.example.vestrapay.utils.dtos.Response;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IRoleService {
    Mono<Response<List<RolePermission>>> createRole(RoleDTO request);
    Mono<Void> createDefaultRole(String userId,String merchantID,String userType);
    Mono<Response<RolePermission>> addPermissionToUser(UpdateRoleDTO request);
    Mono<Response<Void>> removePermissionFromUser(UpdateRoleDTO request);
    Mono<Response<RolePermission>> viewUserRole(String userId);
}
