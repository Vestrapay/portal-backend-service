package com.example.vestrapay.merchant.roles_and_permissions.repository;

import com.example.vestrapay.merchant.roles_and_permissions.models.Permissions;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface PermissionRepository extends R2dbcRepository<Permissions,Long> {
    Mono<Permissions> findByPermissionName(String permissionName);
}
