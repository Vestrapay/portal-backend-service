package com.example.vestrapay.roles_and_permissions.controllers;

import com.example.vestrapay.roles_and_permissions.dtos.RoleDTO;
import com.example.vestrapay.roles_and_permissions.dtos.UpdateRoleDTO;
import com.example.vestrapay.roles_and_permissions.services.PermissionService;
import com.example.vestrapay.roles_and_permissions.services.RoleService;
import com.example.vestrapay.utils.dtos.Response;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/roles")
@Tag(name = "ROLES",description = "Roles and Permission Management")
@RequiredArgsConstructor
@CrossOrigin(origins ="*",maxAge = 3600)
public class RolesController {
    private final PermissionService permissionService;
    private final RoleService roleService;

    @GetMapping("view-all-permissions")
    @PreAuthorize("hasRole('VIEW_ALL_PERMISSIONS')")
    public Mono<ResponseEntity<Response<?>>> viewAllPermissions(){
        return permissionService.getAllPermissions()
                .map(listResponse -> ResponseEntity.status(listResponse.getStatus()).body(listResponse));
    }

    @PostMapping("create-role")
    @PreAuthorize("hasRole('CREATE_ROLES')")
    public Mono<ResponseEntity<Response<?>>> createRole(@RequestBody RoleDTO roleDTO){
        return roleService.createRole(roleDTO)
                .map(listResponse -> ResponseEntity.status(listResponse.getStatus()).body(listResponse));
    }

    @PostMapping("add-permission-to-user")
    @PreAuthorize("hasRole('UPDATE_ROLES')")
    public Mono<ResponseEntity<Response<?>>> addPermissionToUser(@RequestBody UpdateRoleDTO request){
        return roleService.addPermissionToUser(request)
                .map(listResponse -> ResponseEntity.status(listResponse.getStatus()).body(listResponse));
    }

    @PostMapping("remove-permission-from-user")
    @PreAuthorize("hasRole('UPDATE_ROLES')")
    public Mono<ResponseEntity<Response<?>>> removePermissionFromUser(@RequestBody UpdateRoleDTO request){
        return roleService.removePermissionFromUser(request)
                .map(listResponse -> ResponseEntity.status(listResponse.getStatus()).body(listResponse));
    }

}
