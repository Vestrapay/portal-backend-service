package com.example.vestrapay.superadmin.service;

import com.example.vestrapay.users.models.User;
import com.example.vestrapay.utils.dtos.Response;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IAdminService {
    Mono<Response<List<User>>> viewAllAdmin();
    Mono<Response<List<User>>>viewAllMerchants();
    Mono<Response<Boolean>>disableMerchant(String merchantId);
}
