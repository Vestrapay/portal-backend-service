package com.example.vestrapay.superadmin.users.service;

import com.example.vestrapay.merchant.users.models.User;
import com.example.vestrapay.superadmin.users.dto.EnableDisableDTO;
import com.example.vestrapay.superadmin.users.dto.MerchantDTO;
import com.example.vestrapay.utils.dtos.Response;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IAdminService {
    Mono<Response<List<User>>> viewAllAdmin();
    Mono<Response<List<User>>>viewAllMerchants();
    Mono<Response<MerchantDTO>>viewMerchantById(String merchantDTO);
    Mono<Response<Boolean>>disableMerchant(String merchantId);
    Mono<Response<Boolean>>enableMerchant(String merchantId);

    Mono<Response<Object>> enableDisableAdmin(EnableDisableDTO request);
}
