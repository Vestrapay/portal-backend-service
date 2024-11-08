package com.example.vestrapay.merchant.roles_and_permissions.interfaces;

import com.example.vestrapay.merchant.roles_and_permissions.models.Permissions;
import com.example.vestrapay.utils.dtos.Response;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IPermissionService {
    Mono<Response<List<Permissions>>> getAllPermissions();
    Mono<Response<List<Permissions>>> getAllMerchantPermissions();
    Mono<Response<List<Permissions>>> getAllMerchantUserPermissions();
}
