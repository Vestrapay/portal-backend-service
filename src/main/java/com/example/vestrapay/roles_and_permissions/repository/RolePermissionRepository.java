package com.example.vestrapay.roles_and_permissions.repository;

import com.example.vestrapay.roles_and_permissions.models.RolePermission;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RolePermissionRepository extends R2dbcRepository<RolePermission,Long> {
    Flux<RolePermission> findByUserId(String userId);
    Flux<RolePermission> findByUserIdAndMerchantID(String userId,String merchantId);
    Mono<RolePermission>findFirstByUserId(String userId);
    Mono<RolePermission>findByUserIdAndPermissionId(String roleId,String permissionId);

    Mono<RolePermission>findByUserIdAndMerchantIDAndPermissionId(String userID, String merchantID, String permissionID);
    Mono<Void> deleteAllByUserId(String userId);
}
